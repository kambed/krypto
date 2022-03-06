package pl.krypto.backend;

import java.time.format.ResolverStyle;
import java.util.HexFormat;
import java.util.List;

public class Decryptor {
    private ByteArrayOperator bao = new ByteArrayOperator();
    private List<Byte> key;

    public Decryptor(List<Byte> key) {
        this.key = key;
    }

    public byte[] decrypt(byte[] crypt) {
        HexFormat hf = HexFormat.of().withDelimiter(" ");
        System.out.println("START: " + hf.formatHex(crypt));
        byte[] result = new byte[crypt.length];
        for (int blockNumber = 0; blockNumber < crypt.length / 16; blockNumber++) {
            byte[] block = bao.getBlock(blockNumber, crypt);
            block = decryptInitRound(block, key);
            for (int i = 13; i > 0; i--) {
                block = decryptCenterRound(block, key, i);
            }
            block = decryptEndRound(block, key);
            System.out.println("===========================================");
            System.out.println("PLAIN TEXT: " + hf.formatHex(block));
            System.out.println("===========================================");
            for (int i = 0; i < 16; i++) {
                result[16 * blockNumber + i] = block[i];
            }
        }
        System.out.println("DECODED PLAN TEXT LONGER: " + hf.formatHex(result));
        result = bao.remove00fromEnd(result);
        System.out.println("DECODED PLAN TEXT 0 REMOVED: " + hf.formatHex(result));
        return result;
    }

    private byte[] decryptInitRound(byte[] data, List<Byte> key) {
        HexFormat hf = HexFormat.of().withDelimiter(" ");
        byte[] temp;
        temp = bao.addRoundKey(data, bao.get16bytesKeyFragment(14, key));
        System.out.println("AddRoundKey: I=0 (INIT ROUND) " + hf.formatHex(temp));
        temp = bao.invShiftRows(temp);
        System.out.println("InvShiftRows: I=0 (INIT ROUND) " + hf.formatHex(temp));
        temp = bao.changeByteBasedOnInvSbox16(temp);
        System.out.println("InvSubBytes: I=0 (INIT ROUND) " + hf.formatHex(temp));
        return temp;
    }

    private byte[] decryptCenterRound(byte[] data, List<Byte> key, int iteration) {
        HexFormat hf = HexFormat.of().withDelimiter(" ");
        byte[] temp;
        temp = bao.addRoundKey(data, bao.get16bytesKeyFragment(iteration, key));
        System.out.println("AddRoundKey: I=" + (14 - iteration) + " " + hf.formatHex(temp));
        temp = bao.invMixColumns(temp);
        System.out.println("InvMixColumns: I=" + (14 - iteration) + " " + hf.formatHex(temp));
        temp = bao.invShiftRows(temp);
        System.out.println("InvShiftRows: I=" + (14 - iteration) + " " + hf.formatHex(temp));
        temp = bao.changeByteBasedOnInvSbox16(temp);
        System.out.println("InvSubBytes: I=" + (14 - iteration) + " " + hf.formatHex(temp));
        return temp;
    }

    private byte[] decryptEndRound(byte[] data, List<Byte> key) {
        byte[] result;
        result = bao.addRoundKey(data, bao.get16bytesKeyFragment(0, key));
        HexFormat hf = HexFormat.of().withDelimiter(" ");
        System.out.println("AddRoundKey I=14 (INIT ROUND) " + hf.formatHex(result));
        return result;
    }
}
