const MAX_DUMMY_SIZE = 40;
const MAX_DYNAMIC_KEY_SIZE = 8;
const MAX_PADDING_SIZE = 8;

// return random int 0 ~ i
function randomIntValue(i) {
    return Math.floor(Math.random() * (i + 1));
}

function randomBytes(size) {
    const bytes = [];
    for (let i = 0; i < size; ++i) {
        bytes[i] = randomIntValue(255);
        if (bytes[i] > 127)
            bytes[i] -= 255;
    }
    return bytes;
}

export class XorPacker {

    constructor(seedKey, verification
        , maxDummySize = MAX_DUMMY_SIZE
        , maxDynamicKeySize = MAX_DYNAMIC_KEY_SIZE
        , maxPaddingSize = MAX_PADDING_SIZE
        , dynamicLength = true) {
        this.seedKey = seedKey;
        this.verification = verification;
        this.maxDummySize = maxDummySize;
        this.maxDynamicKeySize = maxDynamicKeySize;
        this.maxPaddingSize = maxPaddingSize;
        this.dynamicLength = dynamicLength;
    }

    _enc(key, src) {
        const result = [];
        let pos = 0;
        while (pos < src.length) {
            for (let k of key) {
                result[pos] = src[pos++] ^ k;
                if (pos >= src.length)
                    break
            }
        }
        return result;
    }

    pack(src) {
        if (!src) return null;

        const dynamicKeySize = this.dynamicLength ? randomIntValue(this.maxDynamicKeySize) + 2 : this.maxDynamicKeySize;
        const dummySize = this.dynamicLength ? randomIntValue(this.maxDummySize) + 2 : this.maxDummySize;

        const padding = randomBytes(this.maxPaddingSize);
        const dynamicKey = randomBytes(dynamicKeySize);
        const dummy = randomBytes(dummySize);

        const key = dynamicKey.concat(this.seedKey);
        const encrypted = this._enc(key, src);

        let result = [].concat(padding);
        result[result.length] = dynamicKeySize ^ result[0];
        result = result.concat(dynamicKey);
        result[result.length] = dummySize ^ result[1];

        const v = [];

        for (let i = 0; i < this.verification.length; ++i)
            v[i] = src[Math.abs(this.verification[i] % src.length)];

        return result.concat(v).concat(encrypted).concat(dummy);
    }

    unpack(src) {
        if (!src) return null;

        let dynamicKeySize = src[this.maxPaddingSize];
        if (typeof dynamicKeySize !== 'number') return null;
        dynamicKeySize = dynamicKeySize ^ src[0];
        if (dynamicKeySize < 0) return null;

        const dynamicKey = src.slice(this.maxPaddingSize + 1, this.maxPaddingSize + 1 + dynamicKeySize);

        let dummySize = src[this.maxPaddingSize + 1 + dynamicKeySize];
        if (typeof dummySize !== 'number') return null;
        dummySize = dummySize ^ src[1];
        if (dummySize < 0) return null;

        const v = src.slice(this.maxPaddingSize + 1 + dynamicKeySize + 1, this.maxPaddingSize + 1 + dynamicKeySize + 1 + this.verification.length);

        const encryptedSize = src.length - (this.maxPaddingSize + 1 + dynamicKeySize + 1 + this.verification.length + dummySize);

        const encrypted = src.slice(this.maxPaddingSize + 1 + dynamicKeySize + 1 + this.verification.length
                , this.maxPaddingSize + 1 + dynamicKeySize + 1 + this.verification.length + encryptedSize);

        const key = dynamicKey.concat(this.seedKey);
        const decrypted = this._enc(key, encrypted);

        for (let pos = 0; pos < this.verification.length; ++pos) {
            if (v[pos] !== decrypted[Math.abs(this.verification[pos] % decrypted.length)])
                return null;
        }

        return decrypted;
    }
}