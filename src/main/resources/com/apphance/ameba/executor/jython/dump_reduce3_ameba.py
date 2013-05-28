"""
Apphance Symbolication Utility (Dumper)
Created on 16.01.2013
Written by Wojciech Kedzierski <wkedzierski@utest.com>
"""

import os, sys, logging, subprocess, re, json, zipfile
import plistlib
from optparse import OptionParser

__version__ = 3
__author__ = 'Wojciech Kedzierski <wkedzierski@utest.com>'

DWARFDUMP_PATH = '/usr/bin/dwarfdump'

ASCII_ART = "\n\nWelcome to Apphance Symbolication Utility (Dumper)\n\n\
 .d8b.  d8888b. d8888b. db   db  .d8b.  d8b   db  .o88b. d88888b\n\
d8' `8b 88  `8D 88  `8D 88   88 d8' `8b 888o  88 d8P  Y8 88'    \n\
88ooo88 88oodD' 88oodD' 88ooo88 88ooo88 88V8o 88 8P      88ooooo\n\
88~~~88 88~~~   88~~~   88~~~88 88~~~88 88 V8o88 8b      88~~~~~\n\
88   88 88      88      88   88 88   88 88  V888 Y8b  d8 88.    \n\
YP   YP 88      88      YP   YP YP   YP VP   V8P  `Y88P' Y88888P\n\n"


UUID_REGEXP = "UUID:\s(?P<uuid>\w+-\w+-\w+-\w+-\w+)\s\((?P<arch>\w+)\)"
CATVAL_REGEXP = "AT_(?P<cat>\w+)\(\s(?P<val>.*)\s\)"
SUBPROGRAM_REGEXP = "^(?P<addr>0[xX]+[0-9a-fA-F]+):\s+TAG_subprogram"
SPEC_REGEXP = "{(?P<addr>0[xX]+[0-9a-fA-F]+)}"

def getPLIST(plist_file, verbose = False):
    '''
    Function that parses plist.info file
    :param plist_file: Filepath to plist.info
    :param verbose: Verbose flag
    :return: Plist dictionary
    '''

    logging.info('Getting Application properties')
    if verbose:
        print 'Getting application properties'

    application_properties = plistlib.readPlist(plist_file)

    plist = {}
    plist['Name'] = 'Empty Name'
    plist['CFBundleDisplayName'] = application_properties.get('CFBundleDisplayName')
    plist['CFBundleIdentifier'] = application_properties.get('CFBundleIdentifier')
    plist['CFBundleVersion'] = application_properties.get('CFBundleVersion')
    plist['CFBundleShortVersionString'] = application_properties.get('CFBundleShortVersionString')

    if verbose:
        print '\tApplication Name: %s %s (%s)' %\
              (plist.get('Name'),
               plist.get('CFBundleShortVersionString'),
               plist.get('CFBundleVersion'))

    if verbose:
        print '\tDONE'
    return plist

def getUUID_ARCHS(sym_file, verbose = False):
    '''
    Function that uses dwarfdump to get
    UUIDs and architectures
    from .dsym file
    :param sym_file: .dsym filepath
    :param verbose: Verbose flag
    :return: Table of uuid/archs
    '''

    uuid_archs = []

    logging.info('Getting UUIDs and architectures')
    if verbose:
        print 'Getting UUIDs and architectures'

    dump = subprocess.Popen(['dwarfdump', '-u', sym_file], stdout=subprocess.PIPE)
    output = dump.stdout

    logging.info('Processing dwarfdump - UUIDs')

    for line in output:
        #Expected lines: 'UUID: 28493A88-9076-3588-9266-D5BCF708580E (armv7) exe_helloWorldDesym'
        matched = re.search(UUID_REGEXP,line)
        if matched is not None:
            dsym_uuid = matched.group('uuid')
            dsym_arch = matched.group('arch')

            dsym_uuid = dsym_uuid.replace("-","")
            dsym_uuid = dsym_uuid.lower()

            if verbose: print '\tFound UUID:%s (%s)' % (dsym_uuid, dsym_arch)
            entry = {
                'dsym_uuid' : dsym_uuid,
                'dsym_arch' : dsym_arch,
                }
            uuid_archs.append(entry)

    if verbose:
        print '\tDONE'

    if len(uuid_archs) == 0:
        sys.exit('No UUIDs found')

    return uuid_archs

def entry_has_keys(entry, keys):
    #Return false if entry is too small
    if len(entry) < len(keys):
        return False

    #Return false if entry misses some addresses
    for k in keys:
        if entry.get(k) is None:
            return False

    #Return true after checks
    return True

def entry_has_specificationpointer(entry):
    '''
    Function that checks whether given entry
    contains specification pointer
    :param entry: Dictionary cat/value
    :return: Boolean result
    '''

    #Specification keys
    keys = ['AT_specification']
    return entry_has_keys(entry, keys)

def entry_has_specification(entry):
    '''
    Function that checks whether given entry
    contains specification
    :param entry: Dictionary cat/value
    :return: Boolean result
    '''

    #Specification keys
    keys = ['AT_name', 'AT_decl_file', 'AT_decl_line']
    return entry_has_keys(entry, keys)

def entry_has_addresses(entry):
    '''
    Function that checks whether given entry
    contains addresses
    :param entry: Dictionary cat/value
    :return: Boolean result
    '''

    #Address keys
    keys = ['AT_low_pc', 'AT_high_pc']
    return entry_has_keys(entry, keys)

def entry_is_valid(entry):
    '''
    Function that checks whether given entry
    contains all expected values to be valid
    :param entry: Dictionary cat/value
    :return: Boolean result
    '''

    #Return false if entry has no addresses
    if not entry_has_addresses(entry):
        return False

    #Return false if entry has no specs
    if not entry_has_specification(entry):
        return False

    return True

def additionalSymPass(result, entries_with_nospecifications, entries_with_noaddresses, verbose = False):
    '''
    Function that matches entries with no specification
    with entries with no addresses
    :param result: Table with final results
    :param entries_with_nospecifications: Entries without pointer to specification
    :param entries_with_noaddresses: Entries without address but with specification
    :return: Table with results
    '''
    if verbose: print "\n\tProcessing linked subprograms"


    spec_found_counter = 0

    for entry_sp in entries_with_nospecifications:
        specification_address = entry_sp.get('AT_specification')
        if specification_address is None:
            continue

        specification = entries_with_noaddresses.get(specification_address)
        if specification is None:
            print "Failed to lookup:%s" % specification_address
            continue

        at_name = specification.get('AT_name')
        at_decl_file = specification.get('AT_decl_file')
        at_decl_line = specification.get('AT_decl_line')

        if at_name is None or at_decl_file is None or at_decl_line is None:
            continue

        entry_sp['AT_name'] = at_name
        entry_sp['AT_decl_file'] = at_decl_file
        entry_sp['AT_decl_line'] = at_decl_line

        if entry_is_valid(entry_sp):
            spec_found_counter = spec_found_counter + 1
            result.append(entry_sp)

    if verbose: print "\tDONE - linked subprograms added: %s/%s" % (spec_found_counter, len(entries_with_nospecifications))
    return result

def getSymTable(sym_file, arch, verbose = False):
    '''
    Function that uses dwarfdump to get
    a table of symbols of particular architecture
    from .dsym file
    :param sym_file: File with symbols
    :param arch: Architecture (for example 'armv7')
    :param verbose: Verbose flag
    :return: Table of symbols
    '''

    logging.info('Getting table of symbols for arch:%s', arch)
    print 'Getting table of symbols for: %s' % arch

    dump = subprocess.Popen(['dwarfdump', '--arch', arch, sym_file], stdout=subprocess.PIPE)
    output = dump.stdout

    #processdwarfdump
    logging.info('Processing dwarfdump - subprograms')
    result = []
    entry = {}

    entries_with_nospecifications = []
    entries_with_noaddresses = {}

    process_subprogram = False
    last_cat = False
    current = 0
    counter_subprogram = 0
    counter_closed = 0
    counter_skipped = 0

    logging.info('Iterating through the dwarfdump lines')
    for line in output:
        striped_line = line.strip()

        if verbose: print "\tProcessing %s lines, subprograms found:%s\r" % (current, counter_subprogram),
        current = current + 1

        matched = re.search(SUBPROGRAM_REGEXP, striped_line)
        if matched is not None:
            tag_address = matched.group('addr')
            process_subprogram = True
            counter_subprogram = counter_subprogram + 1

            #Prosess improperly closed entry and skip it- it should be empty
            if len(entry) > 0:
                logging.error('Entry:%s was not closed properly', entry)
                counter_closed = counter_closed + 1
                entry = {}

        # Process lines if a subprogram is found
        # Write values into a dictionary append to a list
        # Watch out on incomplete and invalid entries
        if process_subprogram == True:
            res = re.search(CATVAL_REGEXP,striped_line)
            if res is not None:
                cat = "AT_%s" % res.group('cat')
                val = res.group('val')

                if 'AT_specification' == cat:
                    spec = re.search(SPEC_REGEXP, val)
                    if spec:
                        spec_pointer = spec.group('addr')
                        entry['AT_specification'] = spec_pointer

                elif 'AT_name' == cat:
                    entry['AT_name'] = val

                elif 'AT_decl_file' == cat:
                    entry['AT_decl_file'] = val

                elif 'AT_decl_line' == cat:
                    try:
                        decl_line = int(val)
                        entry['AT_decl_line'] = decl_line
                    except:
                        logging.warning('Int conversion fail for decl line')

                elif 'AT_low_pc' == cat:
                    try:
                        low_pc = int(val,0)
                        entry['AT_low_pc'] = low_pc
                    except:
                        logging.warning('Int conversion fail for low_pc')

                #This is a terminating flag with an address
                elif 'AT_high_pc' == cat:
                    try:
                        high_pc = int(val,0)
                        entry['AT_high_pc'] = high_pc
                    except:
                        logging.warning('Int conversion fail for high_pc')

                #This is a terminating flag without any address
                elif 'AT_inline' == cat:
                    entry['AT_inline'] = val

            if striped_line == '':
                last_cat = True

        #Process closed entry
        if last_cat:
            counter_closed = counter_closed + 1

            has_spec = entry_has_specification(entry)
            has_addr = entry_has_addresses(entry)
            has_ptr = entry_has_specificationpointer(entry)

            #If entry is completely valid
            if has_spec and has_addr:

                result.append(entry)

            #If entry is completely invalid
            if (not has_spec) and (not has_addr):
                counter_skipped = counter_skipped + 1

            #If entry has specification and no address, save for further processing
            if has_spec and (not has_addr):
                entries_with_noaddresses[tag_address] = entry

            #If entry has no specification but has address and pointer to specification
            if (not has_spec) and has_addr and has_ptr:
                entries_with_nospecifications.append(entry)

            #Prepare variables for further processing
            entry = {}
            last_cat = False
            process_subprogram = False

    if verbose:
        print "\n\tDONE - subprograms processed:%s/%s (skipped:%s)" % (counter_closed, counter_subprogram, counter_skipped)

    #Second pass
    #Process entries with pointers on specifications:
    result = additionalSymPass(result, entries_with_nospecifications, entries_with_noaddresses, verbose)

    logging.info("Finished iterating through dwarfdump lines")
    return result

def save_res(header, table, res_file, verbose=False):
    '''
    Function that saves content into a compressed JSON file .ahsym
    :param content: Dictionary to be written
    :param res_file: Output filepath
    :param verbose: Verbose flag
    :return: JSON content written into the file
    '''

    table = sorted(table, key = lambda k: k.get('AT_low_pc'))

    content_raw = {}
    content_raw['dsym_header'] = header
    content_raw['dsym_table'] = table

    logging.info('Dumping content into JSON')
    content_js = json.dumps(content_raw)

    logging.info('Saving output file with JSON content')
    print 'Saving output'
    try:
        output_file = zipfile.ZipFile(res_file,'w',zipfile.ZIP_DEFLATED)
        output_file.writestr('dsym.dict',content_js)
        output_file.close()
    except Exception:
        sys.exit('Writing output to:%s failed' % res_file)

    if  verbose:
        print '\tDONE - %s' % res_file
    return content_js

def generate_filename(filename, arch):
    '''
    This function generates filename for output by injecting architecture
    and adding .ahzip
    :param filename: Input filename
    :param arch: Architecture
    :return:
    '''

    EXT = '.ahsym'
    core = os.path.splitext(filename)[0]
    old_extension = os.path.splitext(filename)[-1]

    if old_extension.lower() == EXT or old_extension.lower() == '':
        filename_with_ext = '%s_%s%s' % (core, arch, EXT)
    else:
        filename_with_ext = '%s_%s%s%s' % (core, arch, old_extension, EXT)

    return filename_with_ext

def parse_and_save(sym_file, dsym_header_plist, res_file, verbose=False):
    '''
    Function that goes through the main logic
    :param sym_file: .dSYM filepath
    :param dsym_header_plist: header from info.plist
    :param res_file: output filename
    :param verbose: Verbose flag
    :return: Table of generated and saved files
    '''

    output_files = []

    #1 Get UUIDS and archs
    uuid_archs = getUUID_ARCHS(sym_file, verbose)
    if uuid_archs is None:
        sys.exit('Getting UUIDs failed')

    #2 For all uuid_archs parse sym_table
    for elem in uuid_archs:
        dsym_uuid = elem.get('dsym_uuid')
        dsym_arch = elem.get('dsym_arch')
        if dsym_uuid is None or dsym_arch is None:
            sys.exit('Getting UUID details failed')

        #2.1 Parse symtable
        sym_table = getSymTable(sym_file, dsym_arch, verbose)
        if sym_table is None:
            sys.exit('Getting sym_table for: %s failed' % dsym_arch)

        #2.2 Prepare header
        header = {}
        header['dump_ver'] = __version__
        header['dsym_uuid'] = dsym_uuid
        header['dsym_arch'] = dsym_arch
        header.update(dsym_header_plist)

        #2.3 Save file
        arch_filename = generate_filename(res_file, dsym_arch)
        output_files.append(arch_filename)
        save_res(header, sym_table, arch_filename, verbose)

    return output_files

def process_plist_symfile(plist_file,sym_file, res_file, verbose=False):
    '''
    Function that processes explicitly given plist and sym files
    :param plist_file: Plist file
    :param sym_file: Sym file
    :param res_file: output filename
    :param verbose: Verbose flag
    :return:
    '''

    if not os.path.exists(plist_file):
        sys.exit('Info.plist filepath given:%s does not exist' % plist_file)

    dsym_header_plist = getPLIST(plist_file, verbose)
    if dsym_header_plist is None:
        sys.exit('Getting Info.plist failed')

    app_name = dsym_header_plist.get('Name')
    if app_name is None:
        sys.exit('Getting Name from Info.plist failed')

    output_files = parse_and_save(sym_file, dsym_header_plist, res_file, verbose)
    return output_files

def process_xcarchive(xcarchive, res_file, verbose=False):
    '''
    Function that processes xcarchive file
    :param xcarchive: Xcode iOS App Archive
    :param res_file: output filename
    :param verbose: Verbose flag
    :return:
    '''
    plist_file = '%s/%s' % (xcarchive,'Info.plist')

    if not os.path.exists(plist_file):
        sys.exit('Info.plist filepath given:%s does not exist' % plist_file)

    dsym_header_plist = getPLIST(plist_file, verbose)
    if dsym_header_plist is None:
        sys.exit('Getting Info.plist failed')

    app_name = dsym_header_plist.get('Name')
    if app_name is None:
        sys.exit('Getting Name from Info.plist failed')

    sym_file = '%s/dSYMs/%s.app.dSYM/Contents/Resources/DWARF/%s' % (xcarchive,
                                                                     app_name.replace(' ',''),
                                                                     app_name.replace(' ',''))

    if not os.path.exists(sym_file):
        sys.exit('DSYM filepath given:%s does not exist' % sym_file)

    output_files = parse_and_save(sym_file, dsym_header_plist, res_file, verbose)
    return output_files

def print_help():
    print ' Apphance uTest 02 Nov 2012,  Wojciech Kedzierski <wkedzierski@utest.com>'
    print ' '
    print ' '
    print ' This util generates a preprocessed table of functions that will be used in crash-symbolication'
    print ' \tProcessing consists of 3 steps:'
    print ' \t\t1) Get version info and UUIDs'
    print ' \t\t2) Get table of symbols for each UUID found'
    print ' \t\t3) Save the outputs'
    print ' '
    print ' Usage example:'
    print ' dump_reduce.py --xcarchive=YourApp.xcarchive --output=YourApp_out'
    print ' '

    return

# This is just a loader while running this locally
class DwarfDumpWrapper(object):
    def __init__(self):
        print "%s" % ASCII_ART

        #Set verbose flag
        verbose = False

        #Set parser options
        parser = OptionParser(add_help_option=False)
        parser.add_option("-h", "--help",
            action="store_true", dest="help")
        parser.add_option("-x", "--xcarchive",
            action="store", dest="xcarchive")
        parser.add_option("-o", "--output",
            action="store", type="string", dest="output")

        parser.add_option("-p", "--plist",
            action="store", type="string", dest="plist")
        parser.add_option("-d", "--dsym",
            action="store", type="string", dest="dsym")

        #Parse options
        (options, args) = parser.parse_args(sys.argv[1:])
        help = options.help
        if help:
            print_help()
            return

        xcarchive = options.xcarchive
        res_file = options.output

        plist = options.plist
        dsym =  options.dsym

        if xcarchive is None:
            if plist and dsym:
                files = process_plist_symfile(plist, dsym, res_file, verbose)
                return
            else:
                sys.exit('Argument missing: --xcarchive not given')

        if not os.path.exists(xcarchive):
            sys.exit('.xcarchive filepath given:%s does not exist' % xcarchive)

        if res_file == None:
            sys.exit('Argument missing: --output not given')

        if not os.path.exists(DWARFDUMP_PATH):
            sys.exit('Dwarfdump tool was not found in path: %s.\nPlease make sure you installed Command Line Tools from Xcode' % DWARFDUMP_PATH)

        #Generate files
        files = process_xcarchive(xcarchive, res_file, verbose)

        return

if __name__ == "__main__":
   DwarfDumpWrapper()
