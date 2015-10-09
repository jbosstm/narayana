import json
import logging

import https_helper
import auth_helper

project_path = '/rest/api/2/project/%s'
issues_path = '/rest/api/2/search?%s'
issue_path = '/rest/api/2/issue/%s'
issue_transitions_path = issue_path + '/transitions'
project_versions_path = '/rest/api/2/project/%s/versions'
project_components_path = '/rest/api/2/project/%s/components'
version_path = '/rest/api/2/version/%s'
issue_types_path = '/rest/api/2/issuetype'


def get_project(jira_host, project_key):
    """
    Returns project based on the key.

    jira_host -- JIRA host to contact.
    project_key -- project key to identify the project.
    """
    response = https_helper.get(jira_host, project_path % project_key)
    if response.status != 200:
        raise ValueError('Failed to get project %s: %s %s' % (project_key, response.status, response.reason))
    return json.loads(response.read())


def get_issues(jira_host, query):
    """
    Returns issues based on the provided search query.

    jira_host -- JIRA host to contact
    query -- search query to filter the issues
    """
    response = https_helper.get(jira_host, issues_path % query)
    if response.status != 200:
        logging.debug('Did not find any issues with the query: %s', query)
        return []
    return json.loads(response.read())['issues']


def update_issue(jira_host, username, password, issue_id, body):
    """
    Updates issue with the provided body.

    jira_host -- JIRA host to contact.
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    issue_id -- id of the issue to update.
    body -- update body.
    """
    headers = get_auth_header(username, password)
    headers.update(get_content_type_header())
    response = https_helper.put(jira_host, issue_path % issue_id, body, headers)
    if response.status != 200:
        ValueError('Failed to update issue %s: %s %s' % (issue_id, response.status, response.reason))


def close_issue(jira_host, username, password, issue_id):
    """
    Closed the specific issue.

    jira_host -- JIRA host to contact.
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    issue_id -- issue to close.
    """
    transitions = get_transitions(jira_host, username, password, issue_id)
    for transition in transitions:
        if transition['name'] == 'Close Issue':
            perform_transition(jira_host, username, password, issue_id, transition['id'])
            return
    raise ValueError('Failed to close issue: %s' % issue_id)


def create_issue(jira_host, username, password, body):
    """
    Creates new JIRA issue

    jira_host -- JIRA host to contact.
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    body -- JSON with new issue body
    """
    headers = get_auth_header(username, password)
    headers.update(get_content_type_header())
    response = https_helper.post(jira_host, issue_path % '', body, headers)
    if response.status != 201:
        raise ValueError('Failed to create issue: status={0}, reason={1}'.format(response.status, response.reason))
    return json.loads(response.read())


def get_issue_type_by_name(jira_host, type_name):
    """
    Returns JIRA issue type based on the name

    jira_host -- JIRA host to contact.
    type_name -- type name to filter.
    """
    response = https_helper.get(jira_host, issue_types_path)
    if response.status != 200:
        raise ValueError('Failed to get issue types: status={0}, reason={1}'.format(response.status, response.reason))
    matches = filter(lambda x: x['name'] == type_name, json.loads(response.read()))
    if not matches:
        raise ValueError('Requested type does not exist: {0}'.format(type_name))
    return matches.pop()


def get_project_versions(jira_host, project_key):
    """
    Returns all versions of the specified project.

    jira_host -- JIRA host to contact.
    project_key -- project which versions should be returned.
    """
    response = https_helper.get(jira_host, project_versions_path % project_key)
    if response.status != 200:
        raise ValueError('Project does not exist: project=%s' % (project_key))
    return json.loads(response.read())


def get_version_by_name(jira_host, project_key, version_name):
    """
    Returns version identified by project and version name.

    jira_host -- JIRA host to contact.
    project_key -- project which version should be returned.
    version_name -- version which should be returned.
    """
    for version in get_project_versions(jira_host, project_key):
        if version['name'] == version_name:
            return version
    raise ValueError('Version does not exist: project=%s, version=%s' % (project_key, version_name))


def create_version(jira_host, username, password, body):
    """
    Creates new version.

    jira_host -- JIRA host to contact.
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    body -- new version body.
    """
    headers = get_auth_header(username, password)
    headers.update(get_content_type_header())
    response = https_helper.post(jira_host, version_path % '', body, headers)
    if response.status != 201:
        raise ValueError('Failed to create version: %s %s' % (response.status, response.reason))


def update_version(jira_host, username, password, version_id, body):
    """
    Updates specific version.

    jira_host -- JIRA host to contact.
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    version_id -- version which should be updated.
    body -- new version body.
    """
    headers = get_auth_header(username, password)
    headers.update(get_content_type_header())
    response = https_helper.put(jira_host, version_path % version_id, body, headers)
    if response.status != 200:
        raise ValueError('Failed to update version %s: %s %s' % (version_id, response.status, response.reason))


def get_project_components(jira_host, project_key):
    """
    Returns all components of the specified project.

    jira_host -- JIRA host to contact.
    project_key -- project which components should be returned.
    """
    response = https_helper.get(jira_host, project_components_path % project_key)
    if response.status != 200:
        raise ValueError('Project does not exist: project={0}'.format(project_key))
    return json.loads(response.read())


def get_component_by_name(jira_host, project_key, name):
    """
    Gets project component based on the name.

    jira_host -- JIRA host to contact.
    project_key -- project which component should be returned.
    name -- name of the component which should be returned
    """
    matches = filter(lambda x: x['name'] == name, get_project_components(jira_host, project_key))
    if not matches:
        raise ValueError('Requested component does not exist: {0}'.format(name))
    return matches.pop()


def get_transitions(jira_host, username, password, issue_id):
    """
    Returns transitions of the issue.

    jira_host -- JIRA host to contact
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    issue_id -- id of the issue which transitions should be returned.
    """
    headers = get_auth_header(username, password)
    response = https_helper.get(jira_host, issue_transitions_path % issue_id, None, headers)
    if response.status != 200:
        logging.debug('Did not find any transitions for issue: %s', issue_id)
        return []
    return json.loads(response.read())['transitions']


def perform_transition(jira_host, username, password, issue_id, transition_id):
    """
    Performs transition on the specified issue.

    jira_host -- JIRA host to contact
    username -- JIRA username with administrative permissions.
    password -- password of the username.
    issue_id -- id of the issue which should be transitioned.
    transition_id -- id of the transition which should be performed.
    """
    headers = get_auth_header(username, password)
    headers.update(get_content_type_header())
    body = json.dumps({'transition': transition_id})
    response = https_helper.post(jira_host, issue_transitions_path % issue_id, body, headers)
    if response.status != 204:
        ValueError('Failed to perform transition on issue %s: %s %s' % (issue_id, response.status, response.reason))


def get_auth_header(username, password):
    return {
        'Authorization': auth_helper.encode_to_auth_header(username, password)
    }


def get_content_type_header():
    return {
        'Content-Type': 'application/json'
    }
