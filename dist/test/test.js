const packer = require('../packer.js');

// console.log(packer);

function arrayEqual(array1, array2) {
    return (array1.length === array2.length) && array1.every(function(element, index) {
        return element === array2[index];
    })
}

const key = [-20, -107, -120, -21, -123, -107, -19, -107, -104, -20, -117, -92, -22, -80, -128, 32, -29, -123, -117, -29, -123, -117, -29, -123, -117, 32, 97, 106, 105, 101];
const v = [-49, -80, 0, -1, 5, -53, -116, 35, -94, 92, 77, -107, -74, 30, -74, -83, 52];
const src = [-36, 70, -55, -84, 19, 125, -112, -8, 123, -14, 37, 38, -7, 43, 89, 56, 18, -44, 31, 65, -7, -83, -82, 88, -59, 9, 36, 16, 44, 40, 121, 80, 64];

const xorPacker = new packer.XorPacker(key, v);

const packed = xorPacker.pack(src);
const unpacked = xorPacker.unpack(packed);

console.log('[XorPacker] pack & unpack result : ' + arrayEqual(src, unpacked));

const srcText = "bas꿻ㄱㄴㄷ😗฿";
const packedUtf8 = xorPacker.packToUTF8(srcText);
console.log('[XorPacker] pack to utf8 : ' + JSON.stringify(packedUtf8));

const unpackedUtf8 = xorPacker.unpackToString([67, -110, -12, -17, -127, -101, -124, -3, 68, -58, 104, 65, 15, -39, -39, 88, -75, -69, -73, 98, 97, -69, -29, -29, -124, -29, -65, -76, -32, -79, -79, -79, -104, -79, -92, 9, 50, -27, 102, 98, -69, 104, 36, 107, 111, 49, 118, 105, 34, 104, 115, 19, 51, 10, 8, 63, -16, -128, 109, -50, -20, -14, 112, 14, -47, 26, -69, 99, -11, -94, -30, 8, -110, 127, -83, 84, 22, 25, -32, 115, -25, 67, 77, 50, 47, 36, 51, -73, -83, -39, -69, 45, -102, 29, -49]);
console.log('[XorPacker] unpack from UTF8 : ' + unpackedUtf8);
console.log('[XorPacker] pack & unpack UTF8 result : ' + (srcText === unpackedUtf8));

