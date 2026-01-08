#include <idl/CosTransactions.idl>
#pragma javaPackage ""


module Demo
{
    exception DemoException {};

    interface DemoInterface : CosTransactions::TransactionalObject
    {
        void work() raises (DemoException);
    };
};