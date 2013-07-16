package com.apphance.flow.plugins.ios.release

import com.apphance.flow.executor.IOSExecutor
import com.apphance.flow.plugins.ios.parsers.PlistParser
import groovy.transform.PackageScope

import javax.inject.Inject
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

import static com.google.common.base.Preconditions.checkArgument
import static groovy.json.JsonOutput.toJson
import static java.lang.Integer.decode
import static org.apache.commons.lang.StringUtils.*
import static org.gradle.api.logging.Logging.getLogger

class IOSDumpReducer {

    private final static UUID_ARCHS_PATTERN = /UUID:\s(\w+-\w+-\w+-\w+-\w+)\s\((\w+)\)/
    private final static SUBPROGRAM_PATTERN = /^(0[xX]+[0-9a-fA-F]+):\s+TAG_subprogram/
    private final static CATVAL_PATTERN = /AT_(\w+)\(\s(.*)\s\)/
    private final static SPEC_PATTERN = /{(0[xX]+[0-9a-fA-F]+)}/

    private logger = getLogger(getClass())

    @Inject PlistParser plistParser
    @Inject IOSExecutor executor

    void reduce(File plist, File dSYM, File outputDir, String name) {
        validatePlist(plist)
        validatedSYM(dSYM)
        validateOutputDir(outputDir)
        validateName(name)

        logger.info("Reducing files in $dSYM.absolutePath, with plist $plist.absolutePath")

        def uuidArchs = UUIDArchs(dSYM)

        uuidArchs.each { e ->
            def dsymUUID = e.dsymUUID
            def dsymArch = e.dsymArch
            checkArgument(isNotEmpty(dsymUUID) && isNotEmpty(dsymArch), 'Getting UUID details failed')

            def symTable = symTable(dSYM, dsymArch)
            symTable = sortList(symTable, 'AT_low_pc')

            def header = [
                    CFBundleDisplayName: plistParser.bundleDisplayName(plist),
                    CFBundleIdentifier: plistParser.bundleId(plist),
                    CFBundleShortVersionString: plistParser.bundleShortVersionString(plist),
                    CFBundleVersion: plistParser.bundleVersion(plist),
                    dsymUUID: dsymUUID,
                    dsymArch: dsymArch,
            ]

            def dsymDict = createDsymDict(outputDir, toJson(['dsym_header': header, dsym_table: symTable]))
            createZipFile(outputDir, "${name}_${dsymArch}", dsymDict)
            dsymDict.delete()
        }
    }

    @PackageScope
    void validatePlist(File plist) {
        checkArgument(plist && plist.exists() && plist.isFile() && plist.size() > 0,
                "Invalid plist file passed: ${plist?.absolutePath}")

    }

    @PackageScope
    void validatedSYM(File dSYMs) {
        checkArgument(dSYMs && dSYMs.exists() && dSYMs.isDirectory() && dSYMs.list().size() > 0,
                "Invalid dSYM dir passed: ${dSYMs?.absolutePath}")
    }

    @PackageScope
    void validateOutputDir(File outputDir) {
        checkArgument(outputDir && outputDir.exists() && outputDir.isDirectory(),
                "Invalid output dir passed: ${outputDir?.absolutePath}")
    }

    @PackageScope
    void validateName(String name) {
        checkArgument(isNotBlank(name), 'Empty name passed')
    }

    @PackageScope
    List<Map<String, String>> UUIDArchs(File dSYM) {
        def uuidArchs = []
        def output = executor.dwarfdumpUUID(dSYM)
        output.collect { it.trim() }.each { line ->
            def matcher = (line =~ UUID_ARCHS_PATTERN)
            if (matcher.find())
                uuidArchs << [dsymUUID: matcher.group(1).toLowerCase().replaceAll('-', ''), dsymArch: matcher.group(2)]
        }
        uuidArchs
    }

    @PackageScope
    List symTable(File dSYM, String dsymArch) {
        def result = []
        def output = executor.dwarfdumpArch(dSYM, dsymArch)

        def entry = [:]
        def entriesWithNoSpecifications = []
        def entriesWithNoAddresses = [:]
        def processSubprogram = false
        def lastCat = false
        def current = 0
        def cntSubprogram = 0
        def cntClosed = 0
        def cntSkipped = 0
        def tagAddress = ''

        output.each { line ->

            line = line.trim()
            current += 1

            def subProgramMatcher = (line =~ SUBPROGRAM_PATTERN)

            if (subProgramMatcher.find()) {
                tagAddress = subProgramMatcher.group(1)
                processSubprogram = true
                cntSubprogram += 1

                if (entry.size > 0) {
                    cntClosed += 1
                    entry = [:]
                }
            }

            if (processSubprogram) {
                def catValMatcher = (line =~ CATVAL_PATTERN)
                if (catValMatcher.find()) {
                    def cat = "AT_${catValMatcher.group(1)}"
                    def val = catValMatcher.group(2)

                    switch (cat) {
                        case 'AT_specification':
                            def specMatcher = (val =~ SPEC_PATTERN)
                            if (specMatcher.find()) {
                                entry['AT_specification'] = specMatcher.group(1)
                            }
                            break
                        case 'AT_name':
                            entry['AT_name'] = val
                            break
                        case 'AT_decl_file':
                            entry['AT_decl_file'] = val
                            break
                        case 'AT_decl_line':
                            entry['AT_decl_line'] = decode(val)
                            break
                        case 'AT_low_pc':
                            entry['AT_low_pc'] = decode(val)
                            break
                        case 'AT_high_pc':
                            entry['AT_high_pc'] = decode(val)
                            break
                        case 'AT_inline':
                            entry['AT_inline'] = val
                            break
                    }
                }
                if (line == '')
                    lastCat = true
            }

            if (lastCat) {
                cntClosed += 1
                def hasSpec = hasSpecification(entry)
                def hasAddr = hasAddresses(entry)
                def hasPtr = hasSpecificationPointer(entry)

                if (hasSpec && hasAddr)
                    result << entry

                if (!hasSpec && !hasAddr)
                    cntSkipped += 1

                if (hasSpec && !hasAddr)
                    entriesWithNoAddresses[tagAddress] = entry

                if (!hasSpec && hasAddr && hasPtr)
                    entriesWithNoSpecifications << entry

                entry = [:]
                lastCat = false
                processSubprogram = false
            }
        }

        logger.info("Sym table for arch '$dsymArch' created. Processed: $cntClosed/$cntSubprogram, skipped: $cntSkipped")

        additionalSymPass(result, entriesWithNoSpecifications, entriesWithNoAddresses)
    }

    @PackageScope
    boolean hasSpecification(Map entry) {
        hasKeys(entry, ['AT_name', 'AT_decl_file', 'AT_decl_line'])
    }

    @PackageScope
    boolean hasAddresses(Map entry) {
        hasKeys(entry, ['AT_low_pc', 'AT_high_pc'])
    }

    @PackageScope
    boolean hasSpecificationPointer(Map entry) {
        hasKeys(entry, ['AT_specification'])
    }

    @PackageScope
    List additionalSymPass(List symTable, List entriesWithNoSpecifications, Map entriesWithNoAddresses) {
        def specFoundCounter = 0

        for (Map entry : entriesWithNoSpecifications) {
            def address = entry['AT_specification'] as String
            if (isEmpty(address))
                continue
            def specification = entriesWithNoAddresses[address]
            if (!specification)
                continue

            def atName = specification.get('AT_name')
            def atDeclFile = specification.get('AT_decl_file')
            def atDeclLine = specification.get('AT_decl_line')

            if (isEmpty(atName) || isEmpty(atDeclFile) || isEmpty(atDeclLine))
                continue

            entry['AT_name'] = atName
            entry['AT_decl_file'] = atDeclFile
            entry['AT_decl_line'] = atDeclLine

            if (hasAddresses(entry) && hasSpecification(entry)) {
                symTable << entry
                specFoundCounter += 1
            }
        }
        logger.info("Linked subprograms added $specFoundCounter/${entriesWithNoSpecifications.size()}")
        symTable
    }

    @PackageScope
    List sortList(List table, String key) {
        table.sort { i, j -> i[key] <=> j[key] }
    }

    @PackageScope
    File createDsymDict(File outputDir, String jsonContent) {
        def dsymDict = new File(outputDir, 'dsym.dict')
        dsymDict.createNewFile()
        dsymDict.text = jsonContent
        dsymDict
    }

    @PackageScope
    void createZipFile(File outputDir, String filename, File dsymDict) {
        def zipFile = new File(outputDir, "${filename}.ahsym")
        def zos = new ZipOutputStream(new FileOutputStream(zipFile))
        zos.putNextEntry(new ZipEntry(dsymDict.name))
        def buffer = new byte[2048]
        dsymDict.withInputStream { i ->
            def l = i.read(buffer)
            if (l > 0) {
                zos.write(buffer, 0, l)
            }
        }
        zos.closeEntry()
        zos.close()
    }

    @PackageScope
    boolean hasKeys(Map entry, List keys) {
        if (entry.size() < keys.size())
            return false

        for (String k : keys) {
            if (isEmpty(entry[k] as String))
                return false
        }

        true
    }
}
