#!/usr/bin/env python

__author__ = 'Gytis Trikleris'


import logging
import json
import os
import re
import calendar
from datetime import datetime

from utils import http_helper
from utils import jira_helper
from utils.input_helper import get_boolean
from utils.input_helper import string_to_boolean


class JobConfig:
    def __init__(self, host, name):
        self.host = host
        self.name = name

    def __str__(self):
        return '<JobConfig: host={0}, name={1}>'.format(self.host, self.name)

    def __repr__(self):
        return self.__str__()


class JiraConfig:
    def __init__(self, host, project, version):
        self.host = host
        self.project = project
        self.version = version

    def __str__(self):
        return '<JiraConfig: host={0}, project={1}, version={2}>'.format(self.host, self.project, self.version)

    def __repr__(self):
        return self.__str__()


def check_jenkins(config):
    """Checks if Jenkins jobs from the list are blue"""
    failures = [config.name for config in config['jenkins_jobs'] if _is_failed_job(config)]
    if failures:
        logging.warn('Following Jenkins jobs have failed: {0}'.format(', '.join(failures)))
    return not failures


def check_jira_blockers(config):
    """Checks if there are any blocker issues in JIRA"""
    blockers = _get_blockers(config['jira'])
    if blockers:
        blocker_names = [blocker['key'] for blocker in blockers]
        logging.warn('Following blockers are still open: {0}'.format(', '.join(blocker_names)))
    return not blockers


def check_deprecated_code(config):
    """Checks if there is deprecated code which should be removed"""
    release_timestamp = _get_major_release_timestamp(config['jira'])
    deprecations = _get_old_deprecations(config['source_path'], release_timestamp)
    if deprecations:
        logging.warn('Following deprecations are older than last major release: {0}'.format(', '.join(deprecations)))
    return not deprecations


def _is_failed_job(config):
    r = http_helper.get(config.host, '/job/{0}/api/json?tree=color'.format(config.name))
    if r.status != 200:
        raise RuntimeError('Failed to get job information for: {0}'.format(config))
    return 'blue' not in json.loads(r.read())['color']


def _get_blockers(config):
    q = 'jql=project={0}+AND+resolution=Unresolved+AND+fixVersion={1}+AND+priority=Blocker' \
        .format(config.project, config.version)
    return jira_helper.get_issues(config.host, q)


def _get_old_deprecations(path, release_timestamp):
    query = 'find {0} -name \*.java | grep -v jboss-as | grep "src/main\|classes" | xargs grep -l "@Deprecated"'.format(
        path)
    return [file_name for file_name in os.popen(query).readlines() if
            release_timestamp > _get_last_commit_timestamp(file_name)]


def _get_major_release_timestamp(config):
    version = [v for v in jira_helper.get_project_versions(config.host, config.project) if
               v['released'] and 'releaseDate' in v and re.search('[0-9].0.0', v['name']) is not None].pop()
    date = datetime.strptime(version['releaseDate'], '%Y-%m-%d')
    return calendar.timegm(date.utctimetuple())


def _get_last_commit_timestamp(file_name):
    query = 'git log -1 --pretty=format:%at -- {0}'.format(file_name)
    return int(os.popen(query).read())


def _get_configuration():
    jenkins_host = os.getenv('JENKINS_HOST', 'narayanaci1.eng.hst.ams2.redhat.com')
    jenkins_jobs = os.getenv('JENKINS_JOBS',
                             'narayana,narayana-catelyn,narayana-quickstarts,narayana-benchmarks').split(',')
    jira_host = os.getenv('JIRA_HOST', 'issues.jboss.org')
    project_key = os.getenv('PROJECT_KEY', 'JBTM')
    current_version = os.getenv('CURRENT_VERSION', '5.next')
    return {
        'jenkins_jobs': (JobConfig(jenkins_host, job) for job in jenkins_jobs),
        'source_path': os.getenv('SOURCE_PATH', '.'),
        'jira': JiraConfig(jira_host, project_key, current_version),
        'fail_deprecation': string_to_boolean(os.getenv('FAIL_DEPRECATION', 'False')),
        'fail_blocker': string_to_boolean(os.getenv('FAIL_BLOCKER', 'True')),
        'fail_jenkins': string_to_boolean(os.getenv('FAIL_JENKINS', 'True')),
        'ask_user': string_to_boolean(os.getenv('ASK_USER', 'True'))
    }


def main():
    config = _get_configuration()
    confirm_message = 'Pre release {0} check failed. Do you want to continue? \n'
    error_message = 'Pre release {0} check failed.'
    checks = [
        {'name': 'jenkins', 'function': check_jenkins, 'fail': config['fail_jenkins']},
        {'name': 'blocker', 'function': check_jira_blockers, 'fail': config['fail_blocker']},
        {'name': 'deprecation', 'function': check_deprecated_code, 'fail': config['fail_deprecation']}
    ]
    for check in checks:
        if not check['function'](config):
            if config['ask_user']:
                if not get_boolean(confirm_message.format(check['name'])):
                    raise ValueError(error_message.format(check['name']))
            elif check['fail']:
                raise ValueError(error_message.format(check['name']))


if __name__ == "__main__":
    main()
