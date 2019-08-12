// This file is part of Origami Editor 3D.
// Copyright (C) 2013, 2014, 2015 Bágyoni Attila <ba-sz-at@users.sourceforge.net>
// Origami Editor 3D is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http:// www.gnu.org/licenses/>.
package origamieditor3d.origami;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Attila Bágyoni (ba-sz-at@users.sourceforge.net)
 * @since 2013-01-14
 */
public class OrigamiIO {

    final static private double[][] Origins = new double[][]{
        new double[]{0, 0, 0},
        new double[]{400, 0, 0},
        new double[]{0, 400, 0},
        new double[]{0, 0, 400}
    };
    
    static public Origami read_gen2(java.io.ByteArrayInputStream ori) {

        try {

            OrigamiGen2 origami;
            ori.reset();
            java.io.InputStream str = origamieditor3d.compression.LZW.extract(ori);

            int fejlec1 = str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                return null;
            } else {

                int fejlec2 = str.read();
                fejlec2 <<= 8;
                fejlec2 += str.read();

                if (fejlec2 != 0x0363) {

                    str.close();
                    return read_gen1(ori);
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new OrigamiGen2(Origami.PaperType.forChar((char) papir));
                        str.read();
                    } else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i = 0; i < sarokszam; i++) {

                            int Xint = str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double) X, (double) Y});
                        }

                        origami = new OrigamiGen2(sarkok);
                    }

                    int parancsfejlec = str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    while (parancsfejlec != 0x0A454f46) {

                        short Xint, Yint, Zint;
                        int Xfrac, Yfrac, Zfrac;

                        Xint = (short) str.read();
                        Xint <<= 8;
                        Xint += str.read();
                        Xfrac = str.read();
                        Xfrac <<= 8;
                        Xfrac += str.read();
                        double X = Xint + Math.signum(Xint) * (double) Xfrac / 256 / 256;

                        Yint = (short) str.read();
                        Yint <<= 8;
                        Yint += str.read();
                        Yfrac = str.read();
                        Yfrac <<= 8;
                        Yfrac += str.read();
                        double Y = Yint + Math.signum(Yint) * (double) Yfrac / 256 / 256;

                        Zint = (short) str.read();
                        Zint <<= 8;
                        Zint += str.read();
                        Zfrac = str.read();
                        Zfrac <<= 8;
                        Zfrac += str.read();
                        double Z = Zint + Math.signum(Zint) * (double) Zfrac / 256 / 256;

                        double[] sikpont = new double[3];
                        double[] siknv = new double[3];
                        sikpont[0] = (double) X + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][0];
                        sikpont[1] = (double) Y + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][1];
                        sikpont[2] = (double) Z + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][2];
                        siknv[0] = X;
                        siknv[1] = Y;
                        siknv[2] = Z;

                        //térfélválasztás
                        if (((parancsfejlec >>> 24) - ((parancsfejlec >>> 24) % 32)) / 32 == 1) {

                            siknv = new double[]{-siknv[0], -siknv[1], -siknv[2]};
                        }

                        double[] parancs;
                        if ((parancsfejlec >>> 24) % 8 == 1) {

                            //ref. fold
                            parancs = new double[7];
                            parancs[0] = 1;
                        } else if ((parancsfejlec >>> 24) % 8 == 2) {

                            //positive rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 3) {

                            //negative rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = -(parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 4) {

                            //partial ref. fold
                            parancs = new double[8];
                            parancs[0] = 3;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 5) {

                            //positive partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 6) {

                            //negative partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (double) -(parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 7) {

                            //crease
                            parancs = new double[7];
                            parancs[0] = 5;
                        } else if (parancsfejlec % 65536 == 65535) {

                            //cut
                            parancs = new double[7];
                            parancs[0] = 6;
                        } else {

                            //partial cut
                            parancs = new double[8];
                            parancs[0] = 7;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        }

                        parancs[1] = sikpont[0];
                        parancs[2] = sikpont[1];
                        parancs[3] = sikpont[2];
                        parancs[4] = siknv[0];
                        parancs[5] = siknv[1];
                        parancs[6] = siknv[2];

                        origami.history.add(parancs);

                        parancsfejlec = str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                    }
                    origami.redoAll();
                    str.close();
                    return origami;
                }
            }
        } catch (Exception ex) {
            return null;
        }
    }

    static public Origami read_gen1(java.io.ByteArrayInputStream ori) {

        try {

            Origami origami;
            ori.reset();
            java.io.InputStream str = origamieditor3d.compression.LZW.extract(ori);

            int fejlec1 = str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();
            fejlec1 <<= 8;
            fejlec1 += str.read();

            if (fejlec1 != 0x4f453344) {

                str.close();
                return null;
            } else {

                int fejlec2 = str.read();
                fejlec2 <<= 8;
                fejlec2 += str.read();

                if (fejlec2 != 0x0263) {

                    str.close();
                    return null;
                } else {

                    int papir = str.read();

                    if (Origami.PaperType.forChar((char) papir) != Origami.PaperType.Custom) {

                        origami = new Origami(Origami.PaperType.forChar((char) papir));
                        str.read();
                    } else {

                        ArrayList<double[]> sarkok = new ArrayList<>(Arrays.asList(new double[][]{}));
                        int sarokszam = str.read();

                        for (int i = 0; i < sarokszam; i++) {

                            int Xint = str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            Xint <<= 8;
                            Xint += str.read();
                            float X = Float.intBitsToFloat(Xint);

                            int Yint = str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            Yint <<= 8;
                            Yint += str.read();
                            float Y = Float.intBitsToFloat(Yint);

                            sarkok.add(new double[]{(double) X, (double) Y});
                        }

                        origami = new Origami(sarkok);
                    }

                    int parancsfejlec = str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    parancsfejlec <<= 8;
                    parancsfejlec += str.read();
                    while (parancsfejlec != 0x0A454f46) {

                        short Xint, Yint, Zint;
                        int Xfrac, Yfrac, Zfrac;

                        Xint = (short) str.read();
                        Xint <<= 8;
                        Xint += str.read();
                        Xfrac = str.read();
                        Xfrac <<= 8;
                        Xfrac += str.read();
                        double X = Xint + Math.signum(Xint) * (double) Xfrac / 256 / 256;

                        Yint = (short) str.read();
                        Yint <<= 8;
                        Yint += str.read();
                        Yfrac = str.read();
                        Yfrac <<= 8;
                        Yfrac += str.read();
                        double Y = Yint + Math.signum(Yint) * (double) Yfrac / 256 / 256;

                        Zint = (short) str.read();
                        Zint <<= 8;
                        Zint += str.read();
                        Zfrac = str.read();
                        Zfrac <<= 8;
                        Zfrac += str.read();
                        double Z = Zint + Math.signum(Zint) * (double) Zfrac / 256 / 256;

                        double[] sikpont = new double[3];
                        double[] siknv = new double[3];
                        sikpont[0] = (double) X + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][0];
                        sikpont[1] = (double) Y + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][1];
                        sikpont[2] = (double) Z + Origins[(((parancsfejlec >>> 24) % 32) - ((parancsfejlec >>> 24) % 8)) / 8][2];
                        siknv[0] = X;
                        siknv[1] = Y;
                        siknv[2] = Z;

                        //térfélválasztás
                        if (((parancsfejlec >>> 24) - ((parancsfejlec >>> 24) % 32)) / 32 == 1) {

                            siknv = new double[]{-siknv[0], -siknv[1], -siknv[2]};
                        }

                        double[] parancs;
                        if ((parancsfejlec >>> 24) % 8 == 1) {

                            //ref. fold
                            parancs = new double[7];
                            parancs[0] = 1;
                        } else if ((parancsfejlec >>> 24) % 8 == 2) {

                            //positive rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 3) {

                            //negative rot. fold
                            parancs = new double[8];
                            parancs[0] = 2;
                            parancs[7] = -(parancsfejlec >>> 16) % 256;
                        } else if ((parancsfejlec >>> 24) % 8 == 4) {

                            //partial ref. fold
                            parancs = new double[8];
                            parancs[0] = 3;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 5) {

                            //positive partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 6) {

                            //negative partial rot. fold
                            parancs = new double[9];
                            parancs[0] = 4;
                            parancs[7] = (double) -(parancsfejlec >>> 16) % 256;
                            parancs[8] = (double) (parancsfejlec % 65536);
                        } else if ((parancsfejlec >>> 24) % 8 == 7) {

                            //crease
                            parancs = new double[7];
                            parancs[0] = 5;
                        } else if (parancsfejlec % 65536 == 65535) {

                            //cut
                            parancs = new double[7];
                            parancs[0] = 6;
                        } else {

                            //partial cut
                            parancs = new double[8];
                            parancs[0] = 7;
                            parancs[7] = (double) (parancsfejlec % 65536);
                        }

                        parancs[1] = sikpont[0];
                        parancs[2] = sikpont[1];
                        parancs[3] = sikpont[2];
                        parancs[4] = siknv[0];
                        parancs[5] = siknv[1];
                        parancs[6] = siknv[2];

                        origami.history.add(parancs);

                        parancsfejlec = str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                        parancsfejlec <<= 8;
                        parancsfejlec += str.read();
                    }
                    origami.redoAll();
                    str.close();
                    return origami;
                }
            }
        } catch (Exception ex) {
            return null;
        }
    }
}
