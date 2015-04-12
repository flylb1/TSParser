/*******************************************************************************
 * Copyright 2015 Bin Liu (flylb1@gmail.com)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   
 * http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 *******************************************************************************/
package core;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Iso6937ToUnicode {
    public String convert(char[] data) {
        StringBuffer sb = new StringBuffer();

        int len = data.length;
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            if (c >= 0x00 && c <= 0x7F) {
                sb.append(c);
            } else if (//
            (//
                    (c >= 0xC1 && c <= 0xC8) || (c >= 0xCA && c <= 0xCB) || (c >= 0xCD && c <= 0xCF)) //
                    && (i < (len - 1))//
            ) {
                char combineChar = (char) map1.get((c << 8) | (byte) (data[i + 1])).intValue();
                if (combineChar != 0) {
                    sb.append(combineChar);
                    i++;
                } else {
                    sb.append((char) map0.get(c).intValue());
                }
            } else {
                sb.append((char) map0.get(c).intValue());
            }
        }
        return sb.toString();
    }

    static Map<Integer, Integer> map0 = new HashMap<Integer, Integer>();
    static Map<Integer, Integer> map1 = new HashMap<Integer, Integer>();
    static {
        map0.put(0xA0, 0x00A0); // 10/00 NO-BREAK SPACE
        map0.put(0xA1, 0x00A1); // 10/01 INVERTED EXCLAMATION MARK
        map0.put(0xA2, 0x00A2); // 10/02 CENT SIGN
        map0.put(0xA3, 0x00A3); // 10/03 POUND SIGN
        map0.put(0xA5, 0x00A5); // 10/05 YEN SIGN
        map0.put(0xA7, 0x00A7); // 10/07 SECTION SIGN
        map0.put(0xA8, 0x00A4); // 10/08 CURRENCY SIGN
        map0.put(0xA9, 0x2018); // 10/09 LEFT SINGLE QUOTATION MARK
        map0.put(0xAA, 0x201C); // 10/10 LEFT DOUBLE QUOTATION MARK
        map0.put(0xAB, 0x00AB); // 10/11 LEFT-POINTING DOUBLE ANGLE QUOTATION MARK
        map0.put(0xAC, 0x2190); // 10/12 LEFTWARDS ARROW
        map0.put(0xAD, 0x2191); // 10/13 UPWARDS ARROW
        map0.put(0xAE, 0x2192); // 10/14 RIGHTWARDS ARROW
        map0.put(0xAF, 0x2193); // 10/15 DOWNWARDS ARROW
        map0.put(0xB0, 0x00B0); // 11/00 DEGREE SIGN
        map0.put(0xB1, 0x00B1); // 11/01 PLUS-MINUS SIGN
        map0.put(0xB2, 0x00B2); // 11/02 SUPERSCRIPT TWO
        map0.put(0xB3, 0x00B3); // 11/03 SUPERSCRIPT THREE
        map0.put(0xB4, 0x00D7); // 11/04 MULTIPLICATION SIGN
        map0.put(0xB5, 0x00B5); // 11/05 MICRO SIGN
        map0.put(0xB6, 0x00B6); // 11/06 PILCROW SIGN
        map0.put(0xB7, 0x00B7); // 11/07 MIDDLE DOT
        map0.put(0xB8, 0x00F7); // 11/08 DIVISION SIGN
        map0.put(0xB9, 0x2019); // 11/09 RIGHT SINGLE QUOTATION MARK
        map0.put(0xBA, 0x201D); // 11/10 RIGHT DOUBLE QUOTATION MARK
        map0.put(0xBB, 0x00BB); // 11/11 RIGHT-POINTING DOUBLE ANGLE QUOTATION MARK
        map0.put(0xBC, 0x00BC); // 11/12 VULGAR FRACTION ONE QUARTER
        map0.put(0xBD, 0x00BD); // 11/13 VULGAR FRACTION ONE HALF
        map0.put(0xBE, 0x00BE); // 11/14 VULGAR FRACTION THREE QUARTERS
        map0.put(0xBF, 0x00BF); // 11/15 INVERTED QUESTION MARK
        map0.put(0xD0, 0x2015); // 13/00 HORIZONTAL BAR
        map0.put(0xD1, 0x00B9); // 13/01 SUPERSCRIPT ONE
        map0.put(0xD2, 0x2117); // 13/02 REGISTERED SIGN
        map0.put(0xD3, 0x00A9); // 13/03 COPYRIGHT SIGN
        map0.put(0xD4, 0x00AE); // 13/04 TRADE MARK SIGN
        map0.put(0xD5, 0x266A); // 13/05 EIGHTH NOTE
        map0.put(0xD6, 0x00AC); // 13/06 NOT SIGN
        map0.put(0xD7, 0x00A6); // 13/07 BROKEN BAR
        map0.put(0xDC, 0x215B); // 13/12 VULGAR FRACTION ONE EIGHTH
        map0.put(0xDF, 0x215E); // 13/15 VULGAR FRACTION SEVEN EIGHTHS
        map0.put(0xE0, 0x2126); // 14/00 OHM SIGN
        map0.put(0xE1, 0x00C6); // 14/01 LATIN CAPITAL LETTER AE
        map0.put(0xE2, 0x0110); // 14/02 LATIN CAPITAL LETTER D WITH STROKE
        map0.put(0xE3, 0x00AA); // 14/03 FEMININE ORDINAL INDICATOR
        map0.put(0xE4, 0x0126); // 14/04 LATIN CAPITAL LETTER H WITH STROKE
        map0.put(0xE6, 0x0132); // 14/06 LATIN CAPITAL LIGATURE IJ
        map0.put(0xE7, 0x013F); // 14/07 LATIN CAPITAL LETTER L WITH MIDDLE DOT
        map0.put(0xE8, 0x0141); // 14/08 LATIN CAPITAL LETTER L WITH STROKE
        map0.put(0xE9, 0x00D8); // 14/09 LATIN CAPITAL LETTER O WITH STROKE
        map0.put(0xEA, 0x0152); // 14/10 LATIN CAPITAL LIGATURE OE
        map0.put(0xEB, 0x00BA); // 14/11 MASCULINE ORDINAL INDICATOR
        map0.put(0xEC, 0x00DE); // 14/12 LATIN CAPITAL LETTER THORN
        map0.put(0xED, 0x0166); // 14/13 LATIN CAPITAL LETTER T WITH STROKE
        map0.put(0xEE, 0x014A); // 14/14 LATIN CAPITAL LETTER ENG
        map0.put(0xEF, 0x0149); // 14/15 LATIN SMALL LETTER N PRECEDED BY APOSTROPHE
        map0.put(0xF0, 0x0138); // 15/00 LATIN SMALL LETTER KRA
        map0.put(0xF1, 0x00E6); // 15/01 LATIN SMALL LETTER AE
        map0.put(0xF2, 0x0111); // 15/02 LATIN SMALL LETTER D WITH STROKE
        map0.put(0xF3, 0x00F0); // 15/03 LATIN SMALL LETTER ETH
        map0.put(0xF4, 0x0127); // 15/04 LATIN SMALL LETTER H WITH STROKE
        map0.put(0xF5, 0x0131); // 15/05 LATIN SMALL LETTER DOTLESS I
        map0.put(0xF6, 0x0133); // 15/06 LATIN SMALL LIGATURE IJ
        map0.put(0xF7, 0x0140); // 15/07 LATIN SMALL LETTER L WITH MIDDLE DOT
        map0.put(0xF8, 0x0142); // 15/08 LATIN SMALL LETTER L WITH STROKE
        map0.put(0xF9, 0x00F8); // 15/09 LATIN SMALL LETTER O WITH STROKE
        map0.put(0xFA, 0x0153); // 15/10 LATIN SMALL LIGATURE OE
        map0.put(0xFB, 0x00DF); // 15/11 LATIN SMALL LETTER SHARP S
        map0.put(0xFC, 0x00FE); // 15/12 LATIN SMALL LETTER THORN
        map0.put(0xFD, 0x0167); // 15/13 LATIN SMALL LETTER T WITH STROKE
        map0.put(0xFE, 0x014B); // 15/14 LATIN SMALL LETTER ENG
        map0.put(0xFF, 0x00AD); // 15/15 SOFT HYPHEN$

        map1.put(0xC141, 0x00C0); // LATIN CAPITAL LETTER A WITH GRAVE
        map1.put(0xC145, 0x00C8); // LATIN CAPITAL LETTER E WITH GRAVE
        map1.put(0xC149, 0x00CC); // LATIN CAPITAL LETTER I WITH GRAVE
        map1.put(0xC14F, 0x00D2); // LATIN CAPITAL LETTER O WITH GRAVE
        map1.put(0xC155, 0x00D9); // LATIN CAPITAL LETTER U WITH GRAVE
        map1.put(0xC161, 0x00E0); // LATIN SMALL LETTER A WITH GRAVE
        map1.put(0xC165, 0x00E8); // LATIN SMALL LETTER E WITH GRAVE
        map1.put(0xC169, 0x00EC); // LATIN SMALL LETTER I WITH GRAVE
        map1.put(0xC16F, 0x00F2); // LATIN SMALL LETTER O WITH GRAVE
        map1.put(0xC175, 0x00F9); // LATIN SMALL LETTER U WITH GRAVE
        map1.put(0xC220, 0x00B4); // ACUTE ACCENT
        map1.put(0xC241, 0x00C1); // LATIN CAPITAL LETTER A WITH ACUTE
        map1.put(0xC243, 0x0106); // LATIN CAPITAL LETTER C WITH ACUTE
        map1.put(0xC245, 0x00C9); // LATIN CAPITAL LETTER E WITH ACUTE
        map1.put(0xC249, 0x00CD); // LATIN CAPITAL LETTER I WITH ACUTE
        map1.put(0xC24C, 0x0139); // LATIN CAPITAL LETTER L WITH ACUTE
        map1.put(0xC24E, 0x0143); // LATIN CAPITAL LETTER N WITH ACUTE
        map1.put(0xC24F, 0x00D3); // LATIN CAPITAL LETTER O WITH ACUTE
        map1.put(0xC252, 0x0154); // LATIN CAPITAL LETTER R WITH ACUTE
        map1.put(0xC253, 0x015A); // LATIN CAPITAL LETTER S WITH ACUTE
        map1.put(0xC255, 0x00DA); // LATIN CAPITAL LETTER U WITH ACUTE
        map1.put(0xC259, 0x00DD); // LATIN CAPITAL LETTER Y WITH ACUTE
        map1.put(0xC25A, 0x0179); // LATIN CAPITAL LETTER Z WITH ACUTE
        map1.put(0xC261, 0x00E1); // LATIN SMALL LETTER A WITH ACUTE
        map1.put(0xC263, 0x0107); // LATIN SMALL LETTER C WITH ACUTE
        map1.put(0xC265, 0x00E9); // LATIN SMALL LETTER E WITH ACUTE
        map1.put(0xC267, 0x01F5); // LATIN SMALL LETTER G WITH CEDILLA(4)
        map1.put(0xC269, 0x00ED); // LATIN SMALL LETTER I WITH ACUTE
        map1.put(0xC26C, 0x013A); // LATIN SMALL LETTER L WITH ACUTE
        map1.put(0xC26E, 0x0144); // LATIN SMALL LETTER N WITH ACUTE
        map1.put(0xC26F, 0x00F3); // LATIN SMALL LETTER O WITH ACUTE
        map1.put(0xC272, 0x0155); // LATIN SMALL LETTER R WITH ACUTE
        map1.put(0xC273, 0x015B); // LATIN SMALL LETTER S WITH ACUTE
        map1.put(0xC275, 0x00FA); // LATIN SMALL LETTER U WITH ACUTE
        map1.put(0xC279, 0x00FD); // LATIN SMALL LETTER Y WITH ACUTE
        map1.put(0xC27A, 0x017A); // LATIN SMALL LETTER Z WITH ACUTE
        map1.put(0xC341, 0x00C2); // LATIN CAPITAL LETTER A WITH CIRCUMFLEX
        map1.put(0xC343, 0x0108); // LATIN CAPITAL LETTER C WITH CIRCUMFLEX
        map1.put(0xC345, 0x00CA); // LATIN CAPITAL LETTER E WITH CIRCUMFLEX
        map1.put(0xC347, 0x011C); // LATIN CAPITAL LETTER G WITH CIRCUMFLEX
        map1.put(0xC348, 0x0124); // LATIN CAPITAL LETTER H WITH CIRCUMFLEX
        map1.put(0xC349, 0x00CE); // LATIN CAPITAL LETTER I WITH CIRCUMFLEX
        map1.put(0xC34A, 0x0134); // LATIN CAPITAL LETTER J WITH CIRCUMFLEX
        map1.put(0xC34F, 0x00D4); // LATIN CAPITAL LETTER O WITH CIRCUMFLEX
        map1.put(0xC353, 0x015C); // LATIN CAPITAL LETTER S WITH CIRCUMFLEX
        map1.put(0xC355, 0x00DB); // LATIN CAPITAL LETTER U WITH CIRCUMFLEX
        map1.put(0xC357, 0x0174); // LATIN CAPITAL LETTER W WITH CIRCUMFLEX
        map1.put(0xC359, 0x0176); // LATIN CAPITAL LETTER Y WITH CIRCUMFLEX
        map1.put(0xC361, 0x00E2); // LATIN SMALL LETTER A WITH CIRCUMFLEX
        map1.put(0xC363, 0x0109); // LATIN SMALL LETTER C WITH CIRCUMFLEX
        map1.put(0xC365, 0x00EA); // LATIN SMALL LETTER E WITH CIRCUMFLEX
        map1.put(0xC367, 0x011D); // LATIN SMALL LETTER G WITH CIRCUMFLEX
        map1.put(0xC368, 0x0125); // LATIN SMALL LETTER H WITH CIRCUMFLEX
        map1.put(0xC369, 0x00EE); // LATIN SMALL LETTER I WITH CIRCUMFLEX
        map1.put(0xC36A, 0x0135); // LATIN SMALL LETTER J WITH CIRCUMFLEX
        map1.put(0xC36F, 0x00F4); // LATIN SMALL LETTER O WITH CIRCUMFLEX
        map1.put(0xC373, 0x015D); // LATIN SMALL LETTER S WITH CIRCUMFLEX
        map1.put(0xC375, 0x00FB); // LATIN SMALL LETTER U WITH CIRCUMFLEX
        map1.put(0xC377, 0x0175); // LATIN SMALL LETTER W WITH CIRCUMFLEX
        map1.put(0xC379, 0x0177); // LATIN SMALL LETTER Y WITH CIRCUMFLEX
        map1.put(0xC441, 0x00C3); // LATIN CAPITAL LETTER A WITH TILDE
        map1.put(0xC449, 0x0128); // LATIN CAPITAL LETTER I WITH TILDE
        map1.put(0xC44E, 0x00D1); // LATIN CAPITAL LETTER N WITH TILDE
        map1.put(0xC44F, 0x00D5); // LATIN CAPITAL LETTER O WITH TILDE
        map1.put(0xC455, 0x0168); // LATIN CAPITAL LETTER U WITH TILDE
        map1.put(0xC461, 0x00E3); // LATIN SMALL LETTER A WITH TILDE
        map1.put(0xC469, 0x0129); // LATIN SMALL LETTER I WITH TILDE
        map1.put(0xC46E, 0x00F1); // LATIN SMALL LETTER N WITH TILDE
        map1.put(0xC46F, 0x00F5); // LATIN SMALL LETTER O WITH TILDE
        map1.put(0xC475, 0x0169); // LATIN SMALL LETTER U WITH TILDE
        map1.put(0xC541, 0x0100); // LATIN CAPITAL LETTER A WITH MACRON
        map1.put(0xC545, 0x0112); // LATIN CAPITAL LETTER E WITH MACRON
        map1.put(0xC549, 0x012A); // LATIN CAPITAL LETTER I WITH MACRON
        map1.put(0xC54F, 0x014C); // LATIN CAPITAL LETTER O WITH MACRON
        map1.put(0xC555, 0x016A); // LATIN CAPITAL LETTER U WITH MACRON
        map1.put(0xC561, 0x0101); // LATIN SMALL LETTER A WITH MACRON
        map1.put(0xC565, 0x0113); // LATIN SMALL LETTER E WITH MACRON
        map1.put(0xC569, 0x012B); // LATIN SMALL LETTER I WITH MACRON
        map1.put(0xC56F, 0x014D); // LATIN SMALL LETTER O WITH MACRON
        map1.put(0xC575, 0x016B); // LATIN SMALL LETTER U WITH MACRON
        map1.put(0xC620, 0x02D8); // BREVE
        map1.put(0xC641, 0x0102); // LATIN CAPITAL LETTER A WITH BREVE
        map1.put(0xC647, 0x011E); // LATIN CAPITAL LETTER G WITH BREVE
        map1.put(0xC655, 0x016C); // LATIN CAPITAL LETTER U WITH BREVE
        map1.put(0xC661, 0x0103); // LATIN SMALL LETTER A WITH BREVE
        map1.put(0xC667, 0x011F); // LATIN SMALL LETTER G WITH BREVE
        map1.put(0xC675, 0x016D); // LATIN SMALL LETTER U WITH BREVE
        map1.put(0xC743, 0x010A); // LATIN CAPITAL LETTER C WITH DOT ABOVE
        map1.put(0xC745, 0x0116); // LATIN CAPITAL LETTER E WITH DOT ABOVE
        map1.put(0xC747, 0x0120); // LATIN CAPITAL LETTER G WITH DOT ABOVE
        map1.put(0xC749, 0x0130); // LATIN CAPITAL LETTER I WITH DOT ABOVE
        map1.put(0xC75A, 0x017B); // LATIN CAPITAL LETTER Z WITH DOT ABOVE
        map1.put(0xC763, 0x010B); // LATIN SMALL LETTER C WITH DOT ABOVE
        map1.put(0xC765, 0x0117); // LATIN SMALL LETTER E WITH DOT ABOVE
        map1.put(0xC767, 0x0121); // LATIN SMALL LETTER G WITH DOT ABOVE
        map1.put(0xC77A, 0x017C); // LATIN SMALL LETTER Z WITH DOT ABOVE
        map1.put(0xC820, 0x00A8); // DIAERESIS
        map1.put(0xC841, 0x00C4); // LATIN CAPITAL LETTER A WITH DIAERESIS
        map1.put(0xC845, 0x00CB); // LATIN CAPITAL LETTER E WITH DIAERESIS
        map1.put(0xC849, 0x00CF); // LATIN CAPITAL LETTER I WITH DIAERESIS
        map1.put(0xC84F, 0x00D6); // LATIN CAPITAL LETTER O WITH DIAERESIS
        map1.put(0xC855, 0x00DC); // LATIN CAPITAL LETTER U WITH DIAERESIS
        map1.put(0xC859, 0x0178); // LATIN CAPITAL LETTER Y WITH DIAERESIS
        map1.put(0xC861, 0x00E4); // LATIN SMALL LETTER A WITH DIAERESIS
        map1.put(0xC865, 0x00EB); // LATIN SMALL LETTER E WITH DIAERESIS
        map1.put(0xC869, 0x00EF); // LATIN SMALL LETTER I WITH DIAERESIS
        map1.put(0xC86F, 0x00F6); // LATIN SMALL LETTER O WITH DIAERESIS
        map1.put(0xC875, 0x00FC); // LATIN SMALL LETTER U WITH DIAERESIS
        map1.put(0xC879, 0x00FF); // LATIN SMALL LETTER Y WITH DIAERESIS
        map1.put(0xCA20, 0x02DA); // RING ABOVE
        map1.put(0xCA41, 0x00C5); // LATIN CAPITAL LETTER A WITH RING ABOVE
        map1.put(0xCAAD, 0x016E); // LATIN CAPITAL LETTER U WITH RING ABOVE
        map1.put(0xCA61, 0x00E5); // LATIN SMALL LETTER A WITH RING ABOVE
        map1.put(0xCA75, 0x016F); // LATIN SMALL LETTER U WITH RING ABOVE
        map1.put(0xCB20, 0x00B8); // CEDILLA
        map1.put(0xCB43, 0x00C7); // LATIN CAPITAL LETTER C WITH CEDILLA
        map1.put(0xCB47, 0x0122); // LATIN CAPITAL LETTER G WITH CEDILLA
        map1.put(0xCB4B, 0x0136); // LATIN CAPITAL LETTER K WITH CEDILLA
        map1.put(0xCB4C, 0x013B); // LATIN CAPITAL LETTER L WITH CEDILLA
        map1.put(0xCB4E, 0x0145); // LATIN CAPITAL LETTER N WITH CEDILLA
        map1.put(0xCB52, 0x0156); // LATIN CAPITAL LETTER R WITH CEDILLA
        map1.put(0xCB53, 0x015E); // LATIN CAPITAL LETTER S WITH CEDILLA
        map1.put(0xCB54, 0x0162); // LATIN CAPITAL LETTER T WITH CEDILLA
        map1.put(0xCB63, 0x00E7); // LATIN SMALL LETTER C WITH CEDILLA
        map1.put(0xCB6B, 0x0137); // LATIN SMALL LETTER K WITH CEDILLA
        map1.put(0xCB6C, 0x013C); // LATIN SMALL LETTER L WITH CEDILLA
        map1.put(0xCB6E, 0x0146); // LATIN SMALL LETTER N WITH CEDILLA
        map1.put(0xCB72, 0x0157); // LATIN SMALL LETTER R WITH CEDILLA
        map1.put(0xCB73, 0x015F); // LATIN SMALL LETTER S WITH CEDILLA
        map1.put(0xCB74, 0x0163); // LATIN SMALL LETTER T WITH CEDILLA
        map1.put(0xCD4F, 0x0150); // LATIN CAPITAL LETTER O WITH DOUBLE ACUTE
        map1.put(0xCD55, 0x0170); // LATIN CAPITAL LETTER U WITH DOUBLE ACUTE
        map1.put(0xCD6F, 0x0151); // LATIN SMALL LETTER O WITH DOUBLE ACUTE
        map1.put(0xCD75, 0x0171); // LATIN SMALL LETTER U WITH DOUBLE ACUTE
        map1.put(0xCE20, 0x02DB); // ogonek
        map1.put(0xCE41, 0x0104); // LATIN CAPITAL LETTER A WITH OGONEK
        map1.put(0xCE45, 0x0118); // LATIN CAPITAL LETTER E WITH OGONEK
        map1.put(0xCE49, 0x012E); // LATIN CAPITAL LETTER I WITH OGONEK
        map1.put(0xCE55, 0x0172); // LATIN CAPITAL LETTER U WITH OGONEK
        map1.put(0xCE61, 0x0105); // LATIN SMALL LETTER A WITH OGONEK
        map1.put(0xCE65, 0x0119); // LATIN SMALL LETTER E WITH OGONEK
        map1.put(0xCE69, 0x012F); // LATIN SMALL LETTER I WITH OGONEK
        map1.put(0xCE75, 0x0173); // LATIN SMALL LETTER U WITH OGONEK
        map1.put(0xCF20, 0x02C7); // CARON
        map1.put(0xCF43, 0x010C); // LATIN CAPITAL LETTER C WITH CARON
        map1.put(0xCF44, 0x010E); // LATIN CAPITAL LETTER D WITH CARON
        map1.put(0xCF45, 0x011A); // LATIN CAPITAL LETTER E WITH CARON
        map1.put(0xCF4C, 0x013D); // LATIN CAPITAL LETTER L WITH CARON
        map1.put(0xCF4E, 0x0147); // LATIN CAPITAL LETTER N WITH CARON
        map1.put(0xCF52, 0x0158); // LATIN CAPITAL LETTER R WITH CARON
        map1.put(0xCF53, 0x0160); // LATIN CAPITAL LETTER S WITH CARON
        map1.put(0xCF54, 0x0164); // LATIN CAPITAL LETTER T WITH CARON
        map1.put(0xCF5A, 0x017D); // LATIN CAPITAL LETTER Z WITH CARON
        map1.put(0xCF63, 0x010D); // LATIN SMALL LETTER C WITH CARON
        map1.put(0xCF64, 0x010F); // LATIN SMALL LETTER D WITH CARON
        map1.put(0xCF65, 0x011B); // LATIN SMALL LETTER E WITH CARON
        map1.put(0xCF6C, 0x013E); // LATIN SMALL LETTER L WITH CARON
        map1.put(0xCF6E, 0x0148); // LATIN SMALL LETTER N WITH CARON
        map1.put(0xCF72, 0x0159); // LATIN SMALL LETTER R WITH CARON
        map1.put(0xCF73, 0x0161); // LATIN SMALL LETTER S WITH CARON
        map1.put(0xCF74, 0x0165); // LATIN SMALL LETTER T WITH CARON
        map1.put(0xCF7A, 0x017E); // LATIN SMALL LETTER Z WITH CARON
    }

    public static void main(String[] args) {
        Iso6937ToUnicode iso6937ToUnicode = new Iso6937ToUnicode();
        int[][] ar = { { 0x4F, 0x52, 0x46, 0x20, 0x32, 0x57, 0x20, 0xC2, 0x65, 0x20, 0xC8, 0x69, 0x20, 0xC1, 0x61, 0x20, 0xC8, 0x65 } };

        // System.out.println(Charset.availableCharsets());
        for (int i = 0; i < ar.length; i++) {
            char[] arb = new char[ar[i].length];
            for (int j = 0; j < arb.length; j++) {
                arb[j] = (char) ar[i][j];
            }

            for (int j = 0; j < arb.length; j++) {
                System.out.print(arb[j] + "(" + (int) arb[j] + ")");
            }
            System.out.println();
            String s = iso6937ToUnicode.convert(arb);

            try {
                System.out.println("UTF-16");
                byte[] afterConvert = s.getBytes("UTF-16");
                System.out.println(new String(afterConvert, "UTF-16"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

    }
}
