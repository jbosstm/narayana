/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 *
 * @author JBoss Inc.
 */
package com.arjuna.ats.jbossatx.jts;

import org.omg.CORBA.NVList;
import org.omg.CORBA.*;
import org.omg.CORBA.ORBPackage.InvalidName;

import java.util.Properties;
import java.applet.Applet;

/**
 * Wrapper for external orbs to avoid shutting them down when narayana shuts down its own orbs.
 * External orbs are passed into the following methods:
 * {@link TransactionManagerService#start} and {@ink RecoveryManagerService#start}
 */
class ExternalORBWrapper extends org.omg.CORBA.ORB {
    private final ORB orb;

    public ExternalORBWrapper(ORB orb) {
        this.orb = orb;
    }
    protected void set_parameters(String[] args, Properties props) {
        ;
    }
    protected void set_parameters(Applet app, Properties props) {
        ;
    }
    public void connect(org.omg.CORBA.Object obj) {
        orb.connect(obj);
    }
    public void destroy( ) {
        // do not delegate destroy
    }
    public void disconnect(org.omg.CORBA.Object obj) {
        orb.disconnect(obj);
    }
    public String[] list_initial_services() {
        return orb.list_initial_services();
    }
    public org.omg.CORBA.Object resolve_initial_references(String object_name) throws InvalidName {
        return orb.resolve_initial_references(object_name);
    }
    public String object_to_string(org.omg.CORBA.Object obj) {
        return orb.object_to_string(obj);
    }
    public org.omg.CORBA.Object string_to_object(String str) {
        return orb.string_to_object(str);
    }
    public NVList create_list(int count) {
        return orb.create_list(count);
    }
    public NVList create_operation_list(org.omg.CORBA.Object oper) {
        return orb.create_operation_list(oper);
    }
    public NamedValue create_named_value(String s, Any any, int flags) {
        return orb.create_named_value(s, any, flags);
    }
    public ExceptionList create_exception_list() {
        return orb.create_exception_list();
    }
    public ContextList create_context_list() {
        return orb.create_context_list();
    }
    public Context get_default_context() {
        return orb.get_default_context();
    }
    public Environment create_environment() {
        return orb.create_environment();
    }
    public org.omg.CORBA.portable.OutputStream create_output_stream() {
        return orb.create_output_stream();
    }
    public void send_multiple_requests_oneway(Request[] req) {
        orb.send_multiple_requests_oneway(req);
    }
    public void send_multiple_requests_deferred(Request[] req) {
        orb.send_multiple_requests_deferred(req);
    }
    public boolean poll_next_response() {
        return orb.poll_next_response();
    }
    public Request get_next_response() throws WrongTransaction {
        return orb.get_next_response();
    }
    public TypeCode get_primitive_tc(TCKind tcKind) {
        return orb.get_primitive_tc(tcKind);
    }
    public TypeCode create_struct_tc(String id, String name, StructMember[] members) {
        return orb.create_struct_tc(id, name, members);
    }
    public TypeCode create_union_tc(String id, String name, TypeCode discriminator_type, UnionMember[] members) {
        return orb.create_union_tc(id, name, discriminator_type, members);
    }
    public TypeCode create_enum_tc(String id, String name, String[] members) {
        return orb.create_enum_tc(id, name, members);
    }
    public TypeCode create_alias_tc(String id, String name, TypeCode original_type) {
        return orb.create_alias_tc(id, name, original_type);
    }
    public TypeCode create_exception_tc(String id, String name, StructMember[] members) {
        return orb.create_exception_tc(id, name, members);
    }
    public TypeCode create_interface_tc(String id, String name) {
        return orb.create_interface_tc(id, name);
    }
    public TypeCode create_string_tc(int bound) {
        return orb.create_string_tc(bound);
    }
    public TypeCode create_wstring_tc(int bound) {
        return orb.create_wstring_tc(bound);
    }
    public TypeCode create_sequence_tc(int bound, TypeCode element_type) {
        return orb.create_sequence_tc(bound, element_type);
    }
    public TypeCode create_recursive_sequence_tc(int bound, int offset) {
        return orb.create_recursive_sequence_tc(bound, offset);
    }
    public TypeCode create_array_tc(int length, TypeCode element_type) {
        return orb.create_array_tc(length, element_type);
    }
    public org.omg.CORBA.TypeCode create_native_tc(String id, String name) {
        return orb.create_native_tc(id, name);
    }
    public org.omg.CORBA.TypeCode create_abstract_interface_tc( String id, String name) {
        return orb.create_abstract_interface_tc(id, name);
    }
    public org.omg.CORBA.TypeCode create_fixed_tc(short digits, short scale) {
        return orb.create_fixed_tc(digits, scale);
    }
    public org.omg.CORBA.TypeCode create_value_tc(String id, String name, short type_modifier, TypeCode concrete_base, ValueMember[] members) {
        return orb.create_value_tc(id, name, type_modifier, concrete_base, members);
    }
    public org.omg.CORBA.TypeCode create_recursive_tc(String id) {
        return orb.create_recursive_tc(id);
    }
    public org.omg.CORBA.TypeCode create_value_box_tc(String id, String name, TypeCode boxed_type) {
        return orb.create_value_box_tc(id, name, boxed_type);
    }
    public Any create_any() {
        return orb.create_any();
    }
    public org.omg.CORBA.Current get_current() {
        return orb.get_current();
    }
    public void run() {
        orb.run();
    }
    public void shutdown(boolean wait_for_completion) {
        // do not delegate shutdown
    }
    public boolean work_pending() {
        return orb.work_pending();
    }
    public void perform_work() {
        orb.perform_work();
    }
    public boolean get_service_information(short service_type, ServiceInformationHolder service_info) {
        return orb.get_service_information(service_type, service_info);
    }
    public org.omg.CORBA.DynAny create_dyn_any(org.omg.CORBA.Any value) {
        return orb.create_dyn_any(value);
    }
    public org.omg.CORBA.DynAny create_basic_dyn_any(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_basic_dyn_any(type);
    }
    public org.omg.CORBA.DynStruct create_dyn_struct(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_dyn_struct(type);
    }
    public org.omg.CORBA.DynSequence create_dyn_sequence(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_dyn_sequence(type);
    }
    public org.omg.CORBA.DynArray create_dyn_array(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_dyn_array(type);
    }
    public org.omg.CORBA.DynUnion create_dyn_union(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_dyn_union(type);
    }
    public org.omg.CORBA.DynEnum create_dyn_enum(org.omg.CORBA.TypeCode type) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode {
        return orb.create_dyn_enum(type);
    }
    public org.omg.CORBA.Policy create_policy(int type, org.omg.CORBA.Any val) throws org.omg.CORBA.PolicyError {
        return orb.create_policy(type, val);
    }
}
