package com.apphance.flow.documentation.sites

import com.google.gdata.data.sites.ContentEntry

class AmebaSiteRetrieval {

    public final static String ABSOLUTE_PREFIX = 'https://sites.google.com/a/apphance.com/mobile-build-automation/'
    public final static String SITE_PREFIX = '/a/apphance.com/mobile-build-automation/'
    public final static String IMAGE_FILE_PREFIX = 'images'
    public final static String CSS_FILE_PREFIX = 'css'
    public final static String JS_FILE_PREFIX = 'js'
    public final static String TTF_FILE_PREFIX = 'js'

    private static final CSS_ARRAY = ['.css']
    private static final IMAGE_ARRAY = ['.jpg', '.JPG', '.png', '.PNG', '.gif', '.GIF', '.jpeg', '.JPEG', '.ico', '.ICO']
    private static final JS_ARRAY = ['.js']
    private static final TTF_ARRAY = ['.ttf']

    int imageNumber = 0
    int cssNumber = 0

    private static class PageInfo {
        String href
        String shortName
        String newName
        File localFileName
    }

    private static class Mapping {
        URL url
        File localFileName
    }

    private Map<URL, Mapping> textMap = [:]
    private Map<URL, Mapping> binaryMap = [:]
    private File targetDirectory

    private deleteRecursive(File f) {
        if (f.exists()) {
            f.eachDir({
                println "Deleting directory ${it}"
                deleteRecursive(it)
            });
            f.eachFile {
                println "Deleting file ${it}"
                it.delete()
            }
        }
    }

    private void cleanTargetDirectory(args) {
        println "Cleaning target directory"
        this.targetDirectory = new File(args[2])
        deleteRecursive(targetDirectory)
        targetDirectory.mkdirs()

    }


    private static String convertToRelativePath(File fromFile, File toFile) {
        String fromPath = fromFile.getCanonicalPath()
        String toPath = toFile.getCanonicalPath()
        StringBuilder relativePath = null
        // Thanks to:
        // http://mrpmorris.blogspot.com/2007/05/convert-absolute-path-to-relative-path.html
        fromPath = fromPath.replaceAll("\\\\", "/")
        toPath = toPath.replaceAll("\\\\", "/")
        if (!fromPath.equals(toPath)) {
            String[] fromDirectories = fromPath.split("/");
            String[] toDirectories = toPath.split("/");
            int length = fromDirectories.length < toDirectories.length ?
                fromDirectories.length : toDirectories.length;
            int lastCommonRoot = -1;
            int index;
            for (index = 0; index < length; index++) {
                if (fromDirectories[index].equals(toDirectories[index])) {
                    lastCommonRoot = index;
                } else {
                    break;
                }
            }
            if (lastCommonRoot != -1) {
                relativePath = new StringBuilder();
                for (index = lastCommonRoot + 1; index < fromDirectories.length - 1; index++) {
                    if (fromDirectories[index].length() > 0) {
                        relativePath.append("../");
                    }
                }
                for (index = lastCommonRoot + 1; index < toDirectories.length - 1; index++) {
                    relativePath.append(toDirectories[index] + "/");
                }
                relativePath.append(toDirectories[toDirectories.length - 1]);
            }
        }
        return relativePath == null ? null : relativePath.toString();
    }

    private List readPageInfo(List pages) {
        println "Reading pages information"
        List<PageInfo> pageInfoList = pages.collect { contentEntry ->
            PageInfo pageInfo = new PageInfo()
            pageInfo.href = contentEntry.htmlLink.href
            pageInfo.shortName = contentEntry.htmlLink.href.substring(ABSOLUTE_PREFIX.length())
            if (pageInfo.shortName == 'home') {
                pageInfo.newName = 'index.html'
            } else {
                pageInfo.newName = pageInfo.shortName + '.html'
            }
            pageInfo.localFileName = new File(targetDirectory, pageInfo.newName)
            return pageInfo
        }
        return pageInfoList
    }

    private int countUp(File f) {
        return (f == null) ? 0 : countUp(f.parentFile)
    }

    private String getSiteURL(String shortName) {
        return "${SITE_PREFIX}${shortName}"
    }

    private String getAbsoluteURL(String shortName) {
        return "${ABSOLUTE_PREFIX}${shortName}"
    }

    private matchesAny(String text, array) {
        for (extension in array) {
            if (text.contains(extension)) {
                return [true, extension]
            }
        }
        return [false, null]
    }

    private Mapping putNextMapping(URL context, String stringUrl, String prefix, String extension, Map urlMap) {
        println "Next mapping: context:${context}, stringUrl:${stringUrl}, prefix:${prefix},extension:${extension}"
        File directory = new File(targetDirectory, prefix)
        URL url = new URL(context, stringUrl)
        if (!urlMap.containsKey(url)) {
            println "New mapping"
            Mapping mapping = new Mapping()
            mapping.url = new URL(context, stringUrl)
            mapping.localFileName = new File(directory, prefix + (imageNumber++) + extension)
            urlMap.put(url, mapping)
            return mapping
        } else {
            return urlMap.get(url)
        }
    }

    private Mapping matchUrl(URL context, String stringUrl, boolean binaryOnly) {
        println "Matched url:'${stringUrl}' in context '${context}'"
        def match, extension
        if (!binaryOnly) {
            (match, extension) = matchesAny(stringUrl, CSS_ARRAY)
            if (match) {
                return putNextMapping(context, stringUrl, CSS_FILE_PREFIX, extension, textMap)
            }
            (match, extension) = matchesAny(stringUrl, JS_ARRAY)
            if (match) {
                return putNextMapping(context, stringUrl, JS_FILE_PREFIX, extension, textMap)
            }
        }
        (match, extension) = matchesAny(stringUrl, IMAGE_ARRAY)
        if (match) {
            return putNextMapping(context, stringUrl, IMAGE_FILE_PREFIX, extension, binaryMap)
        }
        (match, extension) = matchesAny(stringUrl, TTF_ARRAY)
        if (match) {
            return putNextMapping(context, stringUrl, TTF_FILE_PREFIX, extension, binaryMap)
        }
        return null
    }

    private String downloadReplacingPatterns(URL context, File localFileName, boolean binaryOnly) {
        String pageText = context.getText('utf-8')
        (pageText =~ /href="([^\s"]*)"/).each { matchString, stringUrl ->
            Mapping mapping = matchUrl(context, stringUrl, binaryOnly)
            if (mapping != null) {
                pageText = pageText.replace(stringUrl, convertToRelativePath(localFileName, mapping.localFileName))
            }
        }
        (pageText =~ /src="([^\s"]*)"/).each { matchString, stringUrl ->
            Mapping mapping = matchUrl(context, stringUrl, binaryOnly)
            if (mapping != null) {
                pageText = pageText.replace(stringUrl, convertToRelativePath(localFileName, mapping.localFileName))
            }
        }
        (pageText =~ /url\('([^\s']*)'\)/).each { matchString, stringUrl ->
            Mapping mapping = matchUrl(context, stringUrl, binaryOnly)
            if (mapping != null) {
                pageText = pageText.replace(stringUrl, convertToRelativePath(localFileName, mapping.localFileName))
            }
        }
        return pageText
    }

    private void downloadPage(List<PageInfo> pageInfoList, PageInfo pageInfo) {
        URL context = new URL(pageInfo.href)
        String pageText = downloadReplacingPatterns(context, pageInfo.localFileName, false)
        pageInfoList.each { pageToReplace ->
            String relativizedPath = convertToRelativePath(pageInfo.localFileName, pageToReplace.localFileName)
            String absolute = getAbsoluteURL(pageToReplace.shortName)
            String site = getSiteURL(pageToReplace.shortName)
            //println "Replacing path: ${pageToReplace.shortName} in page ${pageInfo.shortName}. Relative path: ${relativizedPath}. Absolute: ${absolute}, Site: ${site}"
            pageText = pageText.replaceAll("href=\"${absolute}\"", "href=\"${relativizedPath}\"")
            pageText = pageText.replaceAll("href=\"${site}\"", "href=\"${relativizedPath}\"")
        }
        pageText = pageText.replace('<head>', '<head><meta http-equiv="Content-type" content="text/html;charset=UTF-8">')
        pageInfo.localFileName.append(pageText, 'utf-8')
    }

    public void downloadText(URL url, File file) {
        println "Downloading ${url} to ${file}"
        file.parentFile.mkdirs()
        def text = downloadReplacingPatterns(url, file, true)
        file.append(text, 'utf-8')
    }

    public void downloadBinary(URL url, File file) {
        println "Downloading ${url} to ${file}"
        file.parentFile.mkdirs()
        file.withOutputStream { out ->
            out << url.openStream()
        }
    }


    private downloadPages(List<PageInfo> pageInfoList) {
        pageInfoList.each { pageInfo ->
            println "Downloading page ${pageInfo.shortName}"
            pageInfo.localFileName.parentFile.mkdirs()
            pageInfo.localFileName.delete()
            downloadPage(pageInfoList, pageInfo)
        }
        textMap.each { url, mapping ->
            downloadText(url, mapping.localFileName)
        }
        binaryMap.each { url, mapping ->
            downloadBinary(url, mapping.localFileName)
        }
    }

    public static void main(String[] args) {
        SitesCommunicator sitesCommunicator = new SitesCommunicator()
        sitesCommunicator.readUserPassword(args)
        sitesCommunicator.createService()
        sitesCommunicator.loggingIn()
        List<ContentEntry> pages = sitesCommunicator.retrieveAllPagesIndex()
        AmebaSiteRetrieval siteRetrieval = new AmebaSiteRetrieval()
        siteRetrieval.cleanTargetDirectory(args)
        List<PageInfo> pageInfoList = siteRetrieval.readPageInfo(pages)
        siteRetrieval.downloadPages(pageInfoList)
    }

}
