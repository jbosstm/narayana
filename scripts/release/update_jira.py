#!/usr/bin/env python

import json
import time
import logging
import getpass
from optparse import OptionParser

from utils import jira_helper


def close_resolved_issues(jira_host, username, password, project_key, version_name):
    """
    Close all resolved issues of the specified project and version.

    jira_host -- host address of the JIRA instance to connect to.
    username -- JIRA username with administrative permissions.
    password -- JIRA user password.
    project_key -- key of the project, which issues should be closed.
    version_name -- name of the version, which issues should be closed.
    """
    search_query = 'jql=project=%s+AND+status=resolved+AND+fixVersion=%s&fields=id' % (project_key, version_name)
    for issue in jira_helper.get_issues(jira_host, search_query):
        jira_helper.close_issue(jira_host, username, password, issue['id'])


def move_unresolved_issues(jira_host, username, password, project_key, version_from, version_to):
    """
    Change fixVersion of unresolved issues.

    jira_host -- host address of the JIRA instance to connect to.
    username -- JIRA username with administrative permissions.
    password -- JIRA user password.
    project_key -- key of the project, which issues should be updated.
    version_from -- version to filter unresolved issues.
    version_to -- new version of the unresolved issues.
    """
    search_query = 'jql=project=%s+AND+resolution=unresolved+AND+fixVersion=%s&fields=id' % (project_key, version_from)
    body = json.dumps({"fields": {"fixVersions": [{"name": version_to}]}})
    for issue in jira_helper.get_issues(jira_host, search_query):
        jira_helper.update_issue(jira_host, username, password, issue['id'], body)


def release_version(jira_host, username, password, project_key, temp_version_name, new_version_name):
    """
    Mark specified version as released.

    jira_host -- host address of the JIRA instance to connect to.
    username -- JIRA username with administrative permissions.
    password -- JIRA user password.
    project_key -- key of the project, which version should be released.
    temp_version_name -- currently used version name.
    version_name -- new version name after release.
    """
    version = jira_helper.get_version_by_name(jira_host, project_key, temp_version_name)
    body = json.dumps({'name': new_version_name, 'released': True, 'userReleaseDate': time.strftime('%d/%b/%y')})
    jira_helper.update_version(jira_host, username, password, version['id'], body)


def create_version(jira_host, username, password, project_key, version_name):
    """
    Creates new version.

    jira_host -- host address of the JIRA instance to connect to.
    username -- JIRA username with administration permissions.
    password -- JIRA user password.
    project_key -- project to which version should be added.
    version_name -- name of the new version.
    """
    project = jira_helper.get_project(jira_host, project_key)
    body = json.dumps({'name': version_name, 'projectId': project['id']})
    jira_helper.create_version(jira_host, username, password, body)


def create_component_update_issue():
    pass


def get_password():
    """
    Returns JIRA password from the command prompt.
    """
    return getpass.getpass('JIRA Password: ')


def get_username():
    """
    Returns JIRA username from the command prompt.
    """
    return raw_input('JIRA Username: ')


def get_options():
    """
    Set up possible script arguments and return available options.
    """
    parser = OptionParser()
    parser.add_option('-l', '--log', dest='LOG_LEVEL',
        help='Set log level: CRITICAL, ERROR, WARNING, INFO, DEBUG, NOTSET. Defaults to WARNING',
        choices=['CRITICAL', 'ERROR', 'WARNING', 'INFO', 'DEBUG', 'NOTSET'],
        default='WARNING')
    parser.add_option('-j', '--jira-host', dest='JIRA_HOST',
        help='JIRA host to use during the update. Defaults to issues.jboss.org',
        default='issues.jboss.org')
    parser.add_option('-k', '--project-key', dest='PROJECT_KEY',
        help='JIRA project key to use during the update')
    parser.add_option('-t', '--temp-version', dest='TEMP_VERSION',
        help='Temporal version which should be released. Defaults to 5.next',
        default='5.next')
    parser.add_option('-n', '--new-version', dest='NEW_VERSION',
        help='Actual version of the realse')
    parser.add_option('-u', '--username', dest='USERNAME',
        help='JIRA user name with administration permissions')
    parser.add_option('-p', '--password', dest='PASSWORD',
        help='Password of the JIRA user')
    (options, args) = parser.parse_args()

    if options.PROJECT_KEY is None:
        raise ValueError('Project key is required. Use --help to see options')
    if options.NEW_VERSION is None:
        raise ValueError('New version is required. Use --help to see options')
    if options.USERNAME is None:
        options.USERNAME = get_username()
    if options.PASSWORD is None:
        options.PASSWORD = get_password()

    return options


def setup_logging(log_level):
    logging.basicConfig(level=log_level)


def main(jira_host, project_key, username, password, new_version, temp_version):
    release_version(jira_host, username, password, project_key, temp_version, new_version)
    create_version(jira_host, username, password, project_key, temp_version)
    close_resolved_issues(jira_host, username, password, project_key, new_version)
    move_unresolved_issues(jira_host, username, password, project_key, new_version, temp_version)


if __name__ == "__main__":
    options = get_options()
    setup_logging(options.LOG_LEVEL)
    main(options.JIRA_HOST, options.PROJECT_KEY, options.USERNAME, options.PASSWORD, options.NEW_VERSION,
         options.TEMP_VERSION)
