Client
======

This is the client side implementation of windows integration security.
the project is consist of 2 project:

* Gigaspaces.WIS.Client - windows integrated security client side implementation.
* Gigaspaces.WIS.Client.Example - sample code


Usage
=====

1. add reference of `Gigaspaces.WIS.Client.dll` to your project.
2. add reference of `<project>\Gigaspaces.WIS.Client\lib\Waffle.Windows.AuthProvider.dll` to your project.
3. Now you can use it with your code:
      ```
      ...
      
      String token= new WISClient(host, port, sp).Authenticate();

      var factory = new SpaceProxyFactory("sp");
                
      factory.Credentials = new SecurityContext(WindowsIdentity.GetCurrent().Name, token);

      ISpaceProxy proxy=factory.Create();

      ...
      
      ```
