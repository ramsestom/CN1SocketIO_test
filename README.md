Currently fails on android with the message:

```[main] 0:0:2,582 - [ERROR] Failed to load http://192.168.8.100:3030/socket.io/?EIO=3&transport=polling&t=MAMfYdl: The 'Access-Control-Allow-Origin' header has a value 'null' that is not equal to the supplied origin. Origin 'null' is therefore not allowed access. On line 0 of file:///android_asset/socket_io.html```

If you want to try it, I suggest you to create a simple socket.io server with feathersjs: https://feathersjs.com/ (choose in memory storage, and set DEBUG=* in your console to correctly track communications between your CN1 app and the server) 

To create a socket.io server with feathers, when you have nodejs installed in your computer, it is as simple as installing the feathers CLI with:

> npm install @feathersjs/cli -g

Then create your server with:

> mkdir MyServer

> cd MyServer

> feathers generate app MyServer

and then

> set DEBUG=*

> npm start

and that's it, you would have your socket.io server up and running