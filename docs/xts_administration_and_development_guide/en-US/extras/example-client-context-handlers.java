MyService order = service.getPort(portName, MyService.class);

BindingProvider bindingProvider = (BindingProvider) order;
List<Handler> handlers = new ArrayList<Handler>(1);
handlers.add(new JaxWSHeaderContextProcessor());
bindingProvider.getBinding().setHandlerChain(handlers);