/**
 *
 * Copyright 2005 LogicBlaze Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
#ifndef STOMP_H
#define STOMP_H

#include "apr_general.h"
#include "apr_network_io.h"
#include "apr_hash.h"

#ifdef __cplusplus
extern "C" {
#endif /* __cplusplus */
   
typedef struct stomp_connection {
      apr_socket_t *socket;      
      apr_sockaddr_t *local_sa;
      char *local_ip;      
      apr_sockaddr_t *remote_sa;
      char *remote_ip;	
} stomp_connection;

typedef struct stomp_frame {
   char *command;
   apr_hash_t *headers;
   char *body;
   long body_length;
} stomp_frame;



apr_status_t stomp_connect(stomp_connection **connection_ref, const char *hostname, int port, apr_pool_t *pool);
apr_status_t stomp_disconnect(stomp_connection **connection_ref);

apr_status_t stomp_write(stomp_connection *connection, stomp_frame *frame, apr_pool_t *pool);
apr_status_t stomp_read(stomp_connection *connection, stomp_frame **frame, apr_pool_t *pool);

#ifdef __cplusplus
}
#endif

#endif  /* ! STOMP_H */
