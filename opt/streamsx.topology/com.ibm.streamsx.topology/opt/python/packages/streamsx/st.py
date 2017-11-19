# coding=utf-8
# Licensed Materials - Property of IBM
# Copyright IBM Corp. 2017
import subprocess
import io
import os
"""
IBM Streams utilities using `streamtool`.

Requires a local IBM Streams installation
located by the environment varible `STREAMS_INSTALL`.
"""

if 'STREAMS_INSTALL' in os.environ:
    _install = os.environ['STREAMS_INSTALL']
    _has_local_install = True
else:
    _has_local_install = False


def get_rest_api():
    """
    Get the root URL for the IBM Streams REST API.
    """
    assert _has_local_install

    url=[]
    ok = _run_st(['geturl', '--api'], lines=url)
    if not ok:
        raise ChildProcessError('streamtool geturl')
    return url[0]

def _cancel_job(job_id, force):
    assert _has_local_install

    get_rest_api()

    args = ['canceljob']
    if force:
        args.append('--force')
    args.append(str(job_id))

    return _run_st(args)

def _run_st(args, lines=None):
    args.insert(0, os.path.join(_install, 'bin', 'streamtool'))
    process = subprocess.Popen(args, stdout=subprocess.PIPE, stdin=subprocess.DEVNULL)
    while True:
        line = process.stdout.readline()
        if len(line) == 0:
            process.stdout.close()
            break
        line = line.decode("utf-8").strip()
        if lines is not None:
            lines.append(line)
    process.wait()
    return process.returncode == 0
