import { XorPacker } from './XorPacker.js'
import { TextEncoder, TextDecoder } from 'text-encoding';

XorPacker.prototype.packToUTF8 = function (text) {
    const uint8arr = new TextEncoder("utf-8").encode(text);
    return this.pack(Array.from(new Int8Array(uint8arr)));
};

XorPacker.prototype.unpackToString = function (src) {
    const unpacked = this.unpack(src);
    if (!unpacked) return null;
    return new TextDecoder().decode(new Uint8Array(unpacked));
};

export { XorPacker, TextEncoder, TextDecoder }