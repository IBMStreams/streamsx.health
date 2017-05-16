from distutils.core import setup
setup(
  name = 'streamsx.health',
  packages = ['simulate.*', 'ingest.*'],
  version = '1.0',
  description = 'Utilities for running api course',
  author = 'IBM Streams @ github.com',
  
  url = 'https://github.com/kcibm/IBMStreams/streamsx.health',
  keywords = ['streams', 'ibmstreams', 'streaming', 'demo' ],
  classifiers = [
    'Development Status :: 3 - Alpha',
    'License :: OSI Approved :: Apache Software License',
    'Programming Language :: Python :: 3',
    'Programming Language :: Python :: 3.5',
  ],
  install_requires=[],
)
