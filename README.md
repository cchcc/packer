# packer
Simple. It packs or unpacks bytes or string for interacting between server and client or peer to peer. It encrypts and verifies serialized data.  
Useful at making request/response body for HTTP, packet for TCP/UDP.  
Support Language : java, kotlin

### AesPacker
Nothing special.

### XorPacker
It generates different bytes and length every time for same input data.

### perfomance test
```
cchcc.java.packer.XorPacker - pack and unpack of 100MB : 486 milliseconds
cchcc.java.packer.AesPacker - pack and unpack of 100MB : 1164 milliseconds

cchcc.kt.packer.XorPacker - pack and unpack 100MB : 504 milliseconds
cchcc.kt.packer.AesPacker - pack and unpack 100MB : 1074 milliseconds
```