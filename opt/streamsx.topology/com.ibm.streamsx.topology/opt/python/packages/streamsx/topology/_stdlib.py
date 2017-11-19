# coding=utf-8
#
# Standard library module names:
# https://docs.python.org/3/library/
#
_STD_LIB_MODULES = {

    # 6. Text Processing Services',
    'string',
    're',
    'difflib',
    'textwrap',
    'unicodedata',
    'stringprep',
    'readline',
    'rlcompleter',

    # 7. Binary Data Services'
    'struct',
    'codecs',

    # 8. Data Types',
    'datetime',
    'calendar',
    'collections',
    'collections.abc',
    'heapq',
    'bisect',
    'array',
    'weakref',
    'types',
    'copy',
    'pprint',
    'reprlib',
    'enum',

    # 9. Numeric and Mathematical Modules
    'numbers',
    'math',
    'cmath',
    'decimal',
    'fractions',
    'random',
    'statistics',

    #10. Functional Programming Modules
    'itertools',
    'functools',
    'operator',

    # 11 File and directory access
    'pathlib',
    'os.path',
    'fileinput',
    'stat',
    'filecmp',
    'tempfile',
    'glob',
    'fnmatch',
    'linecache',
    'shutil',
    'macpath',

    # 12 Data Persistence
    'pickle',
    'copyreg',
    'shelve',
    'marshal',
    'dbm',
    'sqlite3',

    # 13 Data Compression and Archiving
    'zlib',
    'gzip',
    'bz2',
    'lzma',
    'zipfile',
    'tarfile',

    # 14. File Formats
    'csv',
    'configparser',
    'netrc',
    'xdrlib',
    'plistlib',

    # 15. Cryptographic Services

    'hashlib',
    'hmac',
    'secrets',

    # 16. Generic Operating System Services
    'os',
    'io',
    'time',
    'argparse',
    'getopt',
    'logging',
    'logging.config',
    'logging.handlers',
    'getpass',
    'curses',
    'curses.textpad',
    'curses.ascii',
    'curses.panel',
    'platform',
    'errno',
    'ctypes',

    # 17. Concurrent Execution
    'threading',
    'multiprocessing',
    'concurrent',
    'concurrent.futures',
    'subprocess',
    'sched',
    'queue',
    'dummy_threading',
    '_thread',
    ' _dummy_thread',

    # 18. Interprocess Communication and Networking
    'socket',
    'ssl',
    'select',
    'selectors',
    'asyncio',
    'asyncore',
    'asynchat',
    'signal',
    'mmap',

    # 19. Internet Data Handling
    'email', 
    'json', 
    'mailcap', 
    'mailbox', 
    'mimetypes', 
    'base64', 
    'binhex', 
    'binascii', 
    'quopri', 
    'uu', 

    # 20. Structured Markup Processing Tools
    'html',
    'html.parser',
    'html.entities',
    'xml.etree.ElementTree',
    'xml.dom',
    'xml.dom.minidom',
    'xml.dom.pulldom',
    'xml.sax',
    'xml.sax.handler',
    'xml.sax.saxutils',
    'xml.sax.xmlreader',
    'xml.parsers.expat',

    # 21. Internet Protocols and Support
    'webbrowser',
    'cgi',
    'cgitb',
    'wsgiref',
    'urllib',
    'http',
    'ftplib',
    'poplib',
    'imaplib',
    'nntplib',
    'smtplib',
    'smtpd',
    'telnetlib',
    'uuid',
    'socketserver',
    'xmlrpc',
    'ipaddress',

    # 22. Multimedia Services
    'audioop',
    'aifc',
    'sunau',
    'wave',
    'chunk',
    'colorsys',
    'imghdr',
    'sndhdr',
    'ossaudiodev',

    # 29. Python Runtime Services
    'sys',
    'sysconfig',
    'builtins',
    'warnings',
    'contextlib',
    'abc',
    'atexit',
    'traceback',
    '__future__',
    'gc',
    'inspect',
    'site',
    'fpectl',

    # 30. Importing modules
    'zipimport',
    'pkgutil',
    'moduleinder',
    'runpy',
    'importlib',

    # 32. Python Language Services
    'parser',
    'ast',
    'symtable',
    'symbol',
    'token',
    'keyword',
    'tokenize',
    'tabnanny',
    'pyclbr',
    'py_compile',
    'compileall',
    'dis',
    'pickletools'
}
