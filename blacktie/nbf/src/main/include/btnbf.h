/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

#ifndef BT_NBF_H
#define BT_NBF_H

#include "blacktieNBFMacro.h"

#ifdef __cplusplus
extern "C" {
#endif

/**
 * This method is used to add a new attribute
 * @buf The buffer to edit
 * @attributeId The attribute to add to
 * @attributeValue The value of the attribute
 * @len The length of the attribute
 * @returns 0 on succcess
 */
extern BLACKTIE_NBF_DLL int btaddattribute(char** buf, char* attributeId, char* attributeValue, int len);

/**
 * This method is used to retrieve a set attribute
 * @buf The buffer to retrieve from
 * @attributeId The attribute to retrieve to
 * @attributeIndex The index of the attribute to retrieve for an array type
 * @attributeValue Memory to place the attribute
 * @len The length of the returned attribute
 * @returns 0 on succcess
 */
extern BLACKTIE_NBF_DLL int btgetattribute(char* buf, char* attributeId, int attributeIndex, char* attributeValue, int* len);

/**
 * This method is used to set an existing attribute
 * @buf The buffer to set to
 * @attributeId The attribute to set to
 * @attributeIndex The index of the attribute to set for an array type
 * @attributeValue The attribute's value
 * @len The length of the attribute
 * @returns 0 on succcess
 */
extern BLACKTIE_NBF_DLL int btsetattribute(char** buf, char* attributeId, int attributeIndex, char* attributeValue, int len);

/**
 * This method is used to remove an existing attribute
 * @buf The buffer to remove from
 * @attributeId The attribute to remove
 * @attributeIndex The index of the attribute to set for an array type
 * @returns 0 on succcess
 */
extern BLACKTIE_NBF_DLL int btdelattribute(char* buf, char* attributeId, int attributeIndex);

/**
 * This method is used to get the max element count
 * @buf The buffer to get
 * @attributeId The attribute to get
 * @returns max counts on success, -1 on failure
 */
extern BLACKTIE_NBF_DLL int btgetoccurs(char* buf, char* attributeId);

#ifdef __cplusplus
}
#endif

#endif
