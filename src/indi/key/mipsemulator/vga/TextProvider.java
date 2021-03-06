package indi.key.mipsemulator.vga;

import indi.key.mipsemulator.controller.component.VgaController;
import indi.key.mipsemulator.core.controller.Machine;
import indi.key.mipsemulator.model.info.BitArray;
import indi.key.mipsemulator.storage.Memory;
import indi.key.mipsemulator.storage.MemoryType;
import indi.key.mipsemulator.util.IoUtils;

public class TextProvider extends ScreenProvider {

    private byte[] asciiStocks;
    private byte[] gbkStocks;

    public TextProvider(Machine machine) {
        super(machine);
        asciiStocks = IoUtils.read("/res/font/ASCII_8.bin");
        gbkStocks = IoUtils.read("/res/font/HZK_16.bin");
    }

    private VgaConfigures.Font preFont = VgaConfigures.Font.EN_8_8;

    @Override
    public void onMemoryChange(Memory memory, int address, int length) {
        VgaConfigures.Font font = VgaConfigures.getFont();
        if (preFont != font) {
            reset();
            preFont = font;
            onMemoryChange(memory, 0, MemoryType.VRAM.getLength());
            return;
        }
        int fontWidth = font.getWidth();
        int fontHeight = font.getHeight();
        if (font.isEN()) {
            for (int i = address - address % 2; i < address + length; i += 4) {
                byte[] bytes = memory.load(i + 2, 2);
                int wordAddr = Byte.toUnsignedInt(bytes[1]) * 8;
                BitArray colorBits = BitArray.of(bytes[0], 8);
                int fb = colorBits.get(0) ? 255 : 0;
                int fg = colorBits.get(1) ? 255 : 0;
                int fr = colorBits.get(2) ? 255 : 0;
                int bb = colorBits.get(4) ? 255 : 0;
                int bg = colorBits.get(5) ? 255 : 0;
                int br = colorBits.get(6) ? 255 : 0;
                drawCharacter(fontWidth, fontHeight, i, asciiStocks, wordAddr,
                        fb, fg, fr, bb, bg, br, 8);
            }
        } else {
            for (int i = address - address % 4; i < address + length; i += 4) {
                byte[] bytes = memory.load(i, 4);
                int offset = (94 * (bytes[0] + 0x60 - 1) + (bytes[1] + 0x60 - 1)) * 32;
                int fb = 255;
                int fg = 255;
                int fr = 255;
                int bb = 0;
                int bg = 0;
                int br = 0;
                drawCharacter(fontWidth, fontHeight, i, gbkStocks, offset,
                        fb, fg, fr, bb, bg, br, 16);
            }
        }

    }

    private void drawCharacter(int fontWidth, int fontHeight, int index, byte[] wordStocks,
                               int address, int fb, int fg, int fr, int bb, int bg, int br, int rawWidth) {
        int numberPerLine = VgaController.WIDTH / fontWidth;
        int indexX = index / 4 % numberPerLine;
        int indexY = index / 4 / numberPerLine;
        int foldX = fontWidth / rawWidth;
        int foldY = fontHeight / rawWidth;
        int marginX = indexX * fontWidth;
        int marginY = indexY * fontHeight;
        byte c;
        for (int offset = 0; offset < rawWidth * rawWidth / 8; offset++) {
            try {
                c = wordStocks[address + offset];
            } catch (Exception e) {
                continue;
            }
            int pointIndex = offset * 8;
            for (int d = 0; d < 8; d++) {
                int pointY = marginY + pointIndex / rawWidth * foldY;
                int pointX = marginX + pointIndex % rawWidth * foldX;
                boolean b = (c & 0x80) == 0;
                for (int m = 0; m < foldX; m++) {
                    for (int n = 0; n < foldY; n++) {
                        if (b) {
                            setRgb(pointY + n, pointX + m, br, bg, bb);
                        } else {
                            setRgb(pointY + n, pointX + m, fr, fg, fb);
                        }
                    }
                }
                c <<= 1;
                pointIndex++;
            }
        }
    }
}
