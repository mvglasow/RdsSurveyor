/*
 RDS Surveyor -- RDS decoder, analyzer and monitor tool and library.
 For more information see
   http://www.jacquet80.eu/
   http://rds-surveyor.sourceforge.net/
 
 Copyright (c) 2009 Christophe Jacquet

 Permission is hereby granted, free of charge, to any person
 obtaining a copy of this software and associated documentation
 files (the "Software"), to deal in the Software without
 restriction, including without limitation the rights to use,
 copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the
 Software is furnished to do so, subject to the following
 conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 OTHER DEALINGS IN THE SOFTWARE.
*/

package eu.jacquet80.rds.core;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


public class RDS {
	private final static int matH[] = {
		0x8000,	0x4000,	0x2000,	0x1000,	0x0800,	0x0400,	0x0200,	0x0100,
		0x0080,	0x0040,	0xB700,	0x5B80,	0x2DC0,	0xA1C0,	0xE7C0,	0xC4C0,
		0xD540,	0xDD80,	0x6EC0,	0x8040,	0xF700, 0x7B80,	0x3DC0,	0xA9C0,
		0xE3C0,	0xC6C0};
	
	private final static int erreurs[] = {
		0x0080401, 0x0010000, 0x0020000, 0x0030000, 0x0040000, 0x0050000, 0x0060000, 0x0000085, 0x0080000, 0x0090000, 0x00A0000, 0x00B0000, 0x00C0000, 0x00D0000, 0x000010A, 0x0000484, 
		0x0100000, 0x0110000, 0x0120000, 0x0130000, 0x0140000, 0x0004024, 0x0160000, 0x0A02000, 0x0180000, 0x0004140, 0x01A0000, 0x0409000, 0x0000214, 0x0002120, 0x0000908, 0x0001110, 
		0x0200000, 0x0210000, 0x0220000, 0x0000300, 0x0240000, 0x00008C0, 0x0260000, 0x0001081, 0x0280000, 0x0008011, 0x0008048, 0x0002402, 0x02C0000, 0x000020A, 0x1404000, 0x0001480, 
		0x0300000, 0x0003040, 0x0008280, 0x0000070, 0x0340000, 0x00000C2, 0x0812000, 0x0802000, 0x0000428, 0x0010428, 0x0004240, 0x0002C00, 0x0001210, 0x0000A08, 0x0002220, 0x0000114, 
		0x0400000, 0x0410000, 0x0420000, 0x0000820, 0x0440000, 0x0100090, 0x0000600, 0x0010600, 0x0480000, 0x0000284, 0x0001180, 0x0080820, 0x0020201, 0x0200101, 0x0000201, 0x0010201, 
		0x0500000, 0x000004C, 0x0000015, 0x0000022, 0x0010090, 0x0000090, 0x0004804, 0x0020090, 0x0580000, 0x0029000, 0x0000414, 0x0009000, 0x2808000, 0x0000128, 0x0002900, 0x0049000, 
		0x0600000, 0x0040500, 0x0006080, 0x000000B, 0x0010500, 0x0000500, 0x00000E0, 0x0008210, 0x0680000, 0x0001280, 0x0000184, 0x000040A, 0x0010101, 0x0000101, 0x1004000, 0x0020101, 
		0x0000850, 0x0001048, 0x0001011, 0x0000809, 0x0004041, 0x0100500, 0x0005800, 0x0800008, 0x0002420, 0x0028004, 0x0001410, 0x0008004, 0x0004440, 0x0002A00, 0x0000228, 0x0048004, 
		0x0800000, 0x0810000, 0x0820000, 0x0000884, 0x0000801, 0x0010801, 0x0001040, 0x2001000, 0x0880000, 0x0000220, 0x0200120, 0x0020220, 0x0000C00, 0x0010C00, 0x0020C00, 0x0400420, 
		0x0900000, 0x0910000, 0x0000508, 0x0000086, 0x0000003, 0x0000034, 0x0020003, 0x0202000, 0x0040402, 0x0000150, 0x0000109, 0x1200080, 0x0000402, 0x0010402, 0x0020402, 0x0005100, 
		0x0A00000, 0x00000C1, 0x0000098, 0x0001880, 0x000002A, 0x0122000, 0x0000044, 0x0102000, 0x0020120, 0x00004C0, 0x0000120, 0x0010120, 0x0009008, 0x0008810, 0x0040120, 0x0182000, 
		0x0B00000, 0x0000608, 0x0052000, 0x0042000, 0x0000828, 0x0022000, 0x0012000, 0x0002000, 0x0002484, 0x0000209, 0x0000250, 0x1000080, 0x0005200, 0x0008012, 0x0092000, 0x0082000, 
		0x0C00000, 0x0C10000, 0x0080A00, 0x0040021, 0x000C100, 0x0020021, 0x0000016, 0x0000021, 0x0020A00, 0x0200900, 0x0000A00, 0x0010A00, 0x00001C0, 0x0020420, 0x0010420, 0x0000420, 
		0x0D00000, 0x0044080, 0x0002500, 0x0240008, 0x0000308, 0x0004080, 0x0000814, 0x0200008, 0x0020202, 0x0048040, 0x0000202, 0x0010202, 0x2008000, 0x0008040, 0x0040202, 0x0028040, 
		0x00010A0, 0x0080900, 0x0002090, 0x0140008, 0x0002022, 0x0120008, 0x0001012, 0x0100008, 0x0008082, 0x0000900, 0x0200A00, 0x0020900, 0x000B000, 0x0040900, 0x1000010, 0x00002C0, 
		0x0004840, 0x0002600, 0x0005001, 0x0040008, 0x0000051, 0x0020008, 0x0010008, 0x0000008, 0x0008880, 0x0000102, 0x0005400, 0x0020102, 0x0000450, 0x0040102, 0x0090008, 0x0080008, 
		0x1000000, 0x1010000, 0x1020000, 0x0002404, 0x1040000, 0x000020C, 0x0001108, 0x0000910, 0x0001002, 0x0011002, 0x0021002, 0x0002005, 0x0002080, 0x0012080, 0x0022080, 0x2800002, 
		0x1100000, 0x00001A0, 0x0000440, 0x2000400, 0x0400240, 0x000C004, 0x0040440, 0x0000112, 0x0001800, 0x0000018, 0x0000041, 0x2000001, 0x0041800, 0x00000C4, 0x0040041, 0x2040001, 
		0x1200000, 0x0000430, 0x1220000, 0x0003400, 0x0000A10, 0x0001208, 0x000010C, 0x0000482, 0x0000006, 0x0000031, 0x0000068, 0x0003001, 0x0040006, 0x0004820, 0x0404000, 0x0000083, 
		0x0008009, 0x0000740, 0x00002A0, 0x0008050, 0x0000212, 0x000D000, 0x2400100, 0x0000C80, 0x0000804, 0x0002042, 0x0020804, 0x0800080, 0x0040804, 0x00010C0, 0x000A200, 0x0000881, 
		0x1400000, 0x0028800, 0x0000182, 0x0008800, 0x0000130, 0x0002204, 0x0003100, 0x0048800, 0x0000054, 0x0102010, 0x0244000, 0x000000D, 0x0000088, 0x0004300, 0x0204000, 0x0214000, 
		0x0040240, 0x0028002, 0x0000980, 0x0008002, 0x0000240, 0x2000200, 0x0020240, 0x0048002, 0x0012010, 0x0002010, 0x0011020, 0x0001020, 0x0080240, 0x0008108, 0x0304000, 0x0041020, 
		0x1600000, 0x0000282, 0x0000C10, 0x0001408, 0x00A4000, 0x0003200, 0x0084000, 0x0000230, 0x0001050, 0x0000848, 0x0044000, 0x0001009, 0x0024000, 0x0034000, 0x0004000, 0x0014000, 
		0x000A001, 0x0000A80, 0x0000412, 0x0040140, 0x00004A0, 0x0020140, 0x2000100, 0x0000140, 0x000A400, 0x000004A, 0x0000013, 0x0000024, 0x00000A1, 0x0000096, 0x0104000, 0x0040024, 
		0x1800000, 0x0088200, 0x1820000, 0x0000111, 0x0101400, 0x000041A, 0x0080042, 0x0004900, 0x0018200, 0x0008200, 0x0040042, 0x0000510, 0x000002C, 0x0048200, 0x0000042, 0x2000002, 
		0x0041400, 0x0084008, 0x0401200, 0x0280080, 0x0001400, 0x0008014, 0x0021400, 0x0004102, 0x0000380, 0x0004008, 0x0040840, 0x0200080, 0x0001001, 0x0011001, 0x0000840, 0x2000800, 
		0x0000211, 0x0004420, 0x0088100, 0x0000248, 0x0004A00, 0x0002440, 0x0480010, 0x1102000, 0x0000610, 0x0004021, 0x0008100, 0x0100080, 0x0001028, 0x0000830, 0x0400010, 0x0003800, 
		0x0040404, 0x00A0080, 0x0090080, 0x0080080, 0x0000404, 0x0009010, 0x0020404, 0x1002000, 0x0030080, 0x0020080, 0x0010080, 0x0000080, 0x0000005, 0x0000032, 0x0020005, 0x0040080, 
		0x0002140, 0x0100104, 0x0101200, 0x0048001, 0x0004120, 0x0028001, 0x0018001, 0x0008001, 0x0004044, 0x0106000, 0x0240010, 0x0048400, 0x0002024, 0x0000310, 0x0200010, 0x0008400, 
		0x0010104, 0x0000104, 0x0001200, 0x0011200, 0x0401400, 0x0040104, 0x0000181, 0x0108001, 0x0016000, 0x0006000, 0x0081200, 0x0026000, 0x2000020, 0x0000060, 0x0000580, 0x000000E, 
		0x0009080, 0x0101100, 0x0004C00, 0x0002240, 0x000A002, 0x0000448, 0x0080010, 0x0004220, 0x00000A2, 0x1000900, 0x0040010, 0x0050010, 0x0020010, 0x0000049, 0x0000010, 0x0010010, 
		0x0011100, 0x0001100, 0x0000204, 0x0010204, 0x000A800, 0x0000281, 0x0040204, 0x1000008, 0x00008A0, 0x0081100, 0x0004003, 0x0400080, 0x0120010, 0x0000680, 0x0100010, 0x000100A, 
		0x2000000, 0x0000040, 0x2020000, 0x0020040, 0x2040000, 0x0040040, 0x0004808, 0x0801000, 0x2080000, 0x0080040, 0x0000418, 0x0001C00, 0x0002210, 0x0000124, 0x0001220, 0x0881000, 
		0x0002004, 0x0100040, 0x0022004, 0x1000400, 0x0042004, 0x0140040, 0x000400A, 0x0002081, 0x0004100, 0x0000288, 0x0024100, 0x1000001, 0x0044100, 0x0000254, 0x000020D, 0x0002480, 
		0x2200000, 0x0200040, 0x0000340, 0x0000805, 0x0000880, 0x0008102, 0x0020880, 0x0800004, 0x0800480, 0x0028008, 0x0018008, 0x0008008, 0x0080880, 0x0001120, 0x0000224, 0x0002110, 
		0x0003000, 0x0013000, 0x0000030, 0x0000007, 0x0000082, 0x0008900, 0x0020082, 0x0900004, 0x0083000, 0x0024200, 0x0000188, 0x0004200, 0x0080082, 0x0404400, 0x0000154, 0x0044200, 
		0x2400000, 0x0400040, 0x0000860, 0x0420040, 0x2440000, 0x0004028, 0x0006800, 0x0000640, 0x0001420, 0x0480040, 0x0002410, 0x020A000, 0x0000218, 0x0001A00, 0x0000904, 0x0000241, 
		0x000000C, 0x001000C, 0x0000062, 0x1400400, 0x00000D0, 0x1000200, 0x0006002, 0x0000089, 0x0008801, 0x0002280, 0x0009040, 0x1400001, 0x0808000, 0x0001202, 0x0000106, 0x0000488, 
		0x0000025, 0x0000012, 0x000004B, 0x0020012, 0x0000540, 0x00200A0, 0x00100A0, 0x00000A0, 0x0000424, 0x002A000, 0x001A000, 0x000A000, 0x0000141, 0x0000A04, 0x0001900, 0x0000118, 
		0x0001008, 0x0000810, 0x0004084, 0x0020810, 0x0014001, 0x0004001, 0x1000100, 0x0024001, 0x0081008, 0x0044400, 0x0002180, 0x010A000, 0x0014400, 0x0004400, 0x0001102, 0x0024400, 
		0x2800000, 0x0800040, 0x0004009, 0x0041000, 0x0000304, 0x0021000, 0x0011000, 0x0001000, 0x0000260, 0x0008090, 0x0004408, 0x00C1000, 0x0006200, 0x0000C40, 0x0091000, 0x0081000, 
		0x00000A8, 0x0900040, 0x0008201, 0x0002880, 0x0488000, 0x0000043, 0x000001A, 0x0101000, 0x0000110, 0x0010110, 0x0008600, 0x0200210, 0x0408000, 0x0000442, 0x0428000, 0x1000800, 
		0x0000081, 0x0010081, 0x0020081, 0x0040004, 0x0001300, 0x0020004, 0x0010004, 0x0000004, 0x0000480, 0x0010480, 0x0020480, 0x0000160, 0x0040480, 0x00A0004, 0x0090004, 0x0080004, 
		0x0024020, 0x0008101, 0x0004020, 0x0014020, 0x0022040, 0x0120004, 0x0002040, 0x0100004, 0x0100480, 0x0008500, 0x0010210, 0x0000210, 0x0608000, 0x0400410, 0x0082040, 0x0040210, 
		0x2C00000, 0x0001600, 0x0000504, 0x000008A, 0x0001820, 0x0000038, 0x0000061, 0x0401000, 0x0148000, 0x0001201, 0x0000105, 0x0000A40, 0x0108000, 0x0000180, 0x0000460, 0x0020180, 
		0x00020A0, 0x0204800, 0x0001090, 0x0000888, 0x0088000, 0x0098000, 0x0002012, 0x0501000, 0x0048000, 0x0058000, 0x0068000, 0x0000242, 0x0008000, 0x0018000, 0x0028000, 0x0038000, 
		0x0014002, 0x0004002, 0x0001500, 0x0024002, 0x0000824, 0x0044002, 0x0080280, 0x0400004, 0x0000940, 0x0000205, 0x0001101, 0x080A000, 0x0020280, 0x0100410, 0x0000280, 0x0004108, 
		0x0014800, 0x0004800, 0x0000094, 0x0024800, 0x0000026, 0x0000011, 0x0000048, 0x2000008, 0x0000142, 0x0040410, 0x000012C, 0x0400210, 0x0208000, 0x0000410, 0x0080048, 0x0008300, 
		0x3000000, 0x1000040, 0x0110400, 0x0100400, 0x3040000, 0x000E000, 0x0000222, 0x0140400, 0x0202800, 0x0001042, 0x0110001, 0x0100001, 0x0100084, 0x00020C0, 0x0009200, 0x0800002, 
		0x0030400, 0x0020400, 0x0010400, 0x0000400, 0x0080084, 0x0400200, 0x0000A20, 0x0040400, 0x0000058, 0x0020001, 0x0010001, 0x0000001, 0x0000084, 0x0010084, 0x0005008, 0x0040001, 
		0x0082800, 0x1200040, 0x0108010, 0x0000190, 0x0802400, 0x0000122, 0x0500100, 0x0408080, 0x0002800, 0x0000046, 0x0010028, 0x0000028, 0x0042800, 0x0009100, 0x0008204, 0x0040028, 
		0x0000700, 0x0220400, 0x0008010, 0x0200400, 0x0081080, 0x0000920, 0x0400100, 0x0240400, 0x0002002, 0x0000844, 0x0022002, 0x0200001, 0x0001080, 0x0011080, 0x000400C, 0x0240001, 
		0x0000422, 0x0080014, 0x0008840, 0x0500400, 0x0110200, 0x0100200, 0x0000490, 0x0120200, 0x0000023, 0x0000014, 0x0004880, 0x0020014, 0x0900020, 0x00000C8, 0x0000091, 0x0204040, 
		0x0000C20, 0x0040200, 0x0008042, 0x0400400, 0x0010200, 0x0000200, 0x0200100, 0x0020200, 0x0000821, 0x00C0200, 0x0001060, 0x0400001, 0x0800020, 0x0080200, 0x0007000, 0x00A0200, 
		0x0008005, 0x0081010, 0x0140100, 0x0048080, 0x0120100, 0x0028080, 0x0100100, 0x0008080, 0x0000808, 0x0001010, 0x0012020, 0x0002020, 0x0040808, 0x0024040, 0x2004000, 0x0004040, 
		0x0060100, 0x0240200, 0x0040100, 0x0050100, 0x0020100, 0x0200200, 0x0000100, 0x0010100, 0x000000A, 0x001000A, 0x0000064, 0x0102020, 0x004000A, 0x0280200, 0x0080100, 0x0090100, 
		0x0004280, 0x0000108, 0x0200208, 0x0020108, 0x0202400, 0x000A010, 0x0090002, 0x0080002, 0x0008240, 0x00000B0, 0x0050002, 0x0040002, 0x0030002, 0x0020002, 0x0010002, 0x0000002, 
		0x0008088, 0x0100108, 0x0000221, 0x0800400, 0x0480020, 0x0001440, 0x0090800, 0x0080800, 0x0004048, 0x0060800, 0x0000620, 0x0040800, 0x0400020, 0x0020800, 0x0010800, 0x0000800, 
		0x0020208, 0x0200108, 0x0000208, 0x0004180, 0x0002400, 0x0012400, 0x0022400, 0x1000004, 0x0042001, 0x0405000, 0x0080208, 0x0002084, 0x0002001, 0x0012001, 0x0022001, 0x0200002, 
		0x002C000, 0x0000121, 0x000C000, 0x001C000, 0x0102400, 0x0000444, 0x004C000, 0x0280800, 0x00200C0, 0x0000520, 0x00000C0, 0x2000080, 0x0000B00, 0x0000045, 0x000001C, 0x0200800, 
		0x0012100, 0x0002100, 0x0202200, 0x0022100, 0x0009800, 0x0008018, 0x0004480, 0x0480002, 0x0014004, 0x0004004, 0x0000890, 0x0001088, 0x0100020, 0x0044004, 0x0004081, 0x0400002, 
		0x0000144, 0x0000A01, 0x2001200, 0x0001240, 0x0080020, 0x0800200, 0x00A0020, 0x0480800, 0x0040020, 0x0000E00, 0x0000092, 0x0440800, 0x0000020, 0x0010020, 0x0020020, 0x0400800, 
		0x0022200, 0x0085000, 0x0002200, 0x0012200, 0x0000408, 0x0010408, 0x0020408, 0x0080050, 0x0015000, 0x0005000, 0x0000502, 0x000008C, 0x0000009, 0x0010009, 0x2000010, 0x0000050, 
		0x0001140, 0x1004800, 0x0000901, 0x0000244, 0x0008006, 0x0A00200, 0x0800100, 0x0810100, 0x0240020, 0x0105000, 0x0000D00, 0x2400080, 0x0200020, 0x0210020, 0x0002014, 0x0000320};
	
	public final static int[] offsetWords = {
		0x0FC, 0x198, 0x168, 0x1B4
	};
	
	public final static String[][] languages = {
		{"Unknown", "??"},
		{"Albanian", "sq"},
		{"Breton", "br"},
		{"Catalan", "ca"},
		{"Croatian", "hr"},
		{"Welsh", "cy"},
		{"Czech", "cs"},
		{"Danish", "da"},
		{"German", "de"},
		{"English", "en"},
		{"Spanish", "es"},
		{"Esperanto", "eo"},
		{"Estonian", "et"},
		{"Basque", "eu"},
		{"Faroese", "fo"},
		{"French", "fr"},
		{"Frisian","fy"},
		{"Irish", "ga"},
		{"Gaelic", "gd"},
		{"Galician", "gl"},
		{"Icelandic", "is"},
		{"Italian", "it"},
		{"Lappish", "-lappish-"},
		{"Latin", "la"},
		{"Latvian", "lv"},
		{"Luxembourgian", "lb"},
		{"Lithuanian", "lt"},
		{"Hungarian", "hu"},
		{"Maltese", "mt"},
		{"Dutch", "nl"},
		{"Norwegian", "nn"},
		{"Occitan", "oc"},
		{"Polish", "pl"},
		{"Portuguese", "pt"},
		{"Romanian", "ro"},
		{"Romansh", "rm"},
		{"Serbian", "sr"},
		{"Slovak", "sk"},
		{"Slovene", "sl"},
		{"Finnish", "fi"},
		{"Swedish", "sv"},
		{"Turkish", "tr"},
		{"Flemish", "-flemish-"},
		{"Walloon", "wa"},
		{"<2C>", "2C"},
		{"<2D>", "2D"},
		{"<2E>", "2E"},
		{"<2F>", "2F"},
		{"<30>", "30"},
		{"<31>", "31"},
		{"<32>", "32"},
		{"<33>", "33"},
		{"<34>", "34"},
		{"<35>", "35"},
		{"<36>", "36"},
		{"<37>", "37"},
		{"<38>", "38"},
		{"<39>", "39"},
		{"Void", "-void-"},
		{"<41>", "41"},
		{"<42>", "42"},
		{"<43>", "43"},
		{"<44>", "44"},
		{"Zulu", "zu"}, 
		{"Vietnamese", "vi"},
		{"Uzbek", "uz"},
		{"Urdu", "ur"},
		{"Ukrainian", "uk"},
		{"Thai", "th"},
		{"Telugu", "te"},
		{"Tatar", "tt"},
		{"Tamil", "ta"},
		{"Tadzhik", "tg"},
		{"Swahili", "sw"},
		{"Sranan Tongo", "-sranan-tongo-"},
		{"Somali", "so"},
		{"Sinhalese", "si"},
		{"Shona", "sn"},
		{"Serbo-Croat", "sh"},
		{"Ruthenian", "-ruthenian-"},
		{"Russian", "ru"},
		{"Quechua", "qu"},
		{"Pushtu", "ps"},
		{"Punjabi", "pa"},
		{"Persian", "fa"},
		{"Papamiento", "-papamiento-"},
		{"Oriya", "or"},
		{"Nepali", "ne"},
		{"Ndebele", "nr"},
		{"Marathi", "mr"},
		{"Moldavian", "mo"},
		{"Malaysian", "ms"},
		{"Malagasay", "mg"},
		{"Macedonian", "mk"},
		{"Laotian", "lo"},
		{"Korean", "ko"},
		{"Khmer", "km"},
		{"Kazakh", "kk"},
		{"Kannada", "kn"},
		{"Japanese", "ja"},
		{"Indonesian", "id"},
		{"Hindi", "hi"},
		{"Hebrew", "he"},
		{"Hausa", "ha"},
		{"Gurani", "gn"},
		{"Gujurati", "gu"},
		{"Greek", "el"},
		{"Georgian", "ka"},
		{"Fulani", "ff"},
		{"Dari", "fa"},
		{"Churash", "cv"},
		{"Chinese", "zh"},
		{"Burmese", "my"},
		{"Bulgarian", "bg"},
		{"Bengali", "bn"},
		{"Belorussian", "be"},
		{"Bambora", "bm"},
		{"Azerbijani", "az"},
		{"Assamese", "as"},
		{"Armenian", "hy"},
		{"Arabic", "ar"},
		{"Amharic", "am"}
	};
	
	private final static String[] ecc_E0 = {"  ", "DE", "DZ", "AD", "IL", "IT", "BE", "RU", "PS", "AL", "AT", "HU", "MT", "DE", "  ", "EG"};
	private final static String[] ecc_E1 = {"  ", "GR", "CY", "SM", "CH", "JO", "FI", "LU", "BG", "DK", "GI", "IQ", "GB", "LY", "RO", "FR"};
	private final static String[] ecc_E2 = {"  ", "MA", "CZ", "PL", "VA", "SK", "SY", "TN", "  ", "LI", "IS", "MC", "LT", "RS/YU", "ES", "NO"};
	private final static String[] ecc_E3 = {"  ", "ME", "IE", "TR", "MK", "TJ", "  ", "  ", "NL", "LV", "LB", "AZ", "HR", "KZ", "SE", "BY"};
	private final static String[] ecc_E4 = {"  ", "MD", "EE", "KG", "  ", "  ", "UA", "KS", "PT", "SI", "AM", "UZ", "GE", "  ", "TM", "BA"};
	private final static String[] ecc_D0 = {"  ", "CM", "DZ/CF", "DJ", "MG", "ML", "AO", "GQ", "GA", "  ", "ZA", "BF", "CG", "TG", "BJ", "MW"};
	private final static String[] ecc_D1 = {"  ", "NA", "LR", "GH", "MR", "CV/ST", "  ", "SN", "GM", "BI", "??", "BW", "KM", "TZ", "ET", "NG"};
	private final static String[] ecc_D2 = {"  ", "SL", "ZW", "MZ", "UG", "SZ", "GN", "SO", "NE", "TD", "GW", "CD", "CI", "  ", "ZM", "ER"};
	private final static String[] ecc_D3 = {"  ", "  ", "  ", "EH", "??", "RW", "LS", "  ", "SC", "  ", "MU", "  ", "SD", "  ", "  ", "  "};
	private final static String[] ecc_A0 = {"US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "US/VI/PR", "  ", "US/VI/PR", "US/VI/PR"};

	private final static String[] ecc_A1 = {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "CA", "CA", "CA", "CA", "GL"};
	private final static String[] ecc_A2 = {"  ", "AI", "AG", "EC", "  ", "BB", "BZ", "KY", "CR", "CU", "AR", "BR", "BM", "AN", "GP", "BS"};
	private final static String[] ecc_A3 = {"  ", "BO", "CO", "JM", "MQ", "GF", "PY", "NI", "  ", "PA", "DM", "DO", "CL", "GD", "  ", "GY"};
	private final static String[] ecc_A4 = {"  ", "GT", "HN", "AW", "  ", "MS", "TT", "PE", "SR", "UY", "KN", "LC", "SV", "HT", "VE", "  "};
	private final static String[] ecc_A5 = {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "MX", "VC", "MX", "MX", "MX/VG"};
	private final static String[] ecc_A6 = {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "PM"};
	private final static String[] ecc_F0 = {"  ", "AU", "AU", "AU", "AU", "AU", "AU", "AU", "AU", "SA", "AF", "MM", "CN", "KP", "BH", "MY"};
	private final static String[] ecc_F1 = {"  ", "KI", "BT", "BD", "PK", "FJ", "OM", "NR", "IR", "NZ", "SB", "BN", "LK", "TW", "KR", "HK"};
	private final static String[] ecc_F2 = {"  ", "KW", "QA", "KH", "WS", "IN", "MO", "VN", "PH", "JP", "SG", "MV", "ID", "AE", "NP", "VU"};
	private final static String[] ecc_F3 = {"  ", "LA", "TH", "TO", "  ", "  ", "  ", "  ", "  ", "PG", "  ", "YE", "  ", "  ", "FM", "MN"};
	private final static String[] ecc_F4 = {"  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  ", "  "};

	
	public final static String getISOCountryCode(int piCC, int ecc) {
		switch(ecc) {
		case 0: return "None";
		case 0xE0: return ecc_E0[piCC];
		case 0xE1: return ecc_E1[piCC];
		case 0xE2: return ecc_E2[piCC];
		case 0xE3: return ecc_E3[piCC];
		case 0xE4: return ecc_E4[piCC];
		case 0xD0: return ecc_D0[piCC];
		case 0xD1: return ecc_D1[piCC];
		case 0xD2: return ecc_D2[piCC];
		case 0xD3: return ecc_D3[piCC];
		case 0xA0: return ecc_A0[piCC];
		case 0xA1: return ecc_A1[piCC];
		case 0xA2: return ecc_A2[piCC];
		case 0xA3: return ecc_A3[piCC];
		case 0xA4: return ecc_A4[piCC];
		case 0xA5: return ecc_A5[piCC];
		case 0xA6: return ecc_A6[piCC];
		case 0xF0: return ecc_F0[piCC];
		case 0xF1: return ecc_F1[piCC];
		case 0xF2: return ecc_F2[piCC];
		case 0xF3: return ecc_F3[piCC];
		case 0xF4: return ecc_F4[piCC];
		default: return "Invalid";
		}
	}
	
	public final static int calcSyndrome(int bloc) {
		int synd = 0;
		for(int i=0; i<26; i++) {
			bloc <<= 1;
			if((bloc & (1<<26)) != 0) synd ^= matH[i];
		}
		return synd;
	}
	
	public static int nbErrors(int syndrome) {
		return poids(erreurs[syndrome]);
	}
	
	public static int correct(int codeword, int syndrome) {
		return codeword ^ erreurs[syndrome];
		//return (codeword - erreurs[syndrome] + 0x4000000) & 0x3FFFFFF;
	}
	
	private static int poids(int codeword) {
		int poids = 0;
		for(int i=0; i<26; i++) {
			if((codeword & 1) != 0) poids++;
			codeword >>= 1;
		}
		return poids;
	}
	
	public static void main(String[] args) throws FileNotFoundException {
		int[] errors = new int[1024];
		for(int cw=1; cw < (1<<26); cw++) {
			int synd = calcSyndrome(cw) >> 6;
			if(errors[synd] == 0) errors[synd] = cw;
			else {
				if(poids(cw) < poids(errors[synd])) errors[synd] = cw;
			}
			if(cw % 1000000 == 0) System.out.println(cw);
		}
		
		PrintWriter w = new PrintWriter(new File("/tmp/errors.txt"));
		for(int s=0; s<1024; s++) {
			w.printf("0x%07X, ", errors[s]);
			if(s%16 == 15) w.println();
		}
		w.flush();
		w.close();
	}
	
	private final static char[] charmap = new char[] {
		  '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020',
		  '\u0020', '\u0020', '\n',   	'\u000B', '\u0020', '\r',	  '\u0020', '\u0020',
		  '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020',
		  '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u0020', '\u001F',	
		  '\u0020',	'\u0021', '\u0022',	'\u0023', '\u00A4',	'\u0025', '\u0026',	'\'',     
		  '\u0028',	'\u0029', '\u002A',	'\u002B', '\u002C',	'\u002D', '\u002E',	'\u002F',	
		  '\u0030',	'\u0031', '\u0032',	'\u0033', '\u0034',	'\u0035', '\u0036',	'\u0037',	
		  '\u0038',	'\u0039', '\u003A',	'\u003B', '\u003C',	'\u003D', '\u003E',	'\u003F',	
		  '\u0040',	'\u0041', '\u0042',	'\u0043', '\u0044',	'\u0045', '\u0046',	'\u0047',	
		  '\u0048',	'\u0049', '\u004A',	'\u004B', '\u004C',	'\u004D', '\u004E',	'\u004F',	
		  '\u0050',	'\u0051', '\u0052',	'\u0053', '\u0054',	'\u0055', '\u0056',	'\u0057',	
		  '\u0058',	'\u0059', '\u005A',	'\u005B', '\\',     '\u005D', '\u2015',	'\u005F',	
		  '\u2551',	'\u0061', '\u0062',	'\u0063', '\u0064',	'\u0065', '\u0066',	'\u0067',	
		  '\u0068',	'\u0069', '\u006A',	'\u006B', '\u006C',	'\u006D', '\u006E',	'\u006F',	
		  '\u0070',	'\u0071', '\u0072',	'\u0073', '\u0074',	'\u0075', '\u0076',	'\u0077',	
		  '\u0078',	'\u0079', '\u007A',	'\u007B', '\u007C',	'\u007D', '\u00AF',	'\u007F',	
		  '\u00E1',	'\u00E0', '\u00E9',	'\u00E8', '\u00ED',	'\u00EC', '\u00F3',	'\u00F2',	
		  '\u00FA',	'\u00F9', '\u00D1',	'\u00C7', '\u015E',	'\u00DF', '\u00A1',	'\u0132',	
		  '\u00E2',	'\u00E4', '\u00EA',	'\u00EB', '\u00EE',	'\u00EF', '\u00F4',	'\u00F6',	
		  '\u00FB',	'\u00FC', '\u00F1',	'\u00E7', '\u015F',	'\u011F', '\u0131',	'\u0133',	
		  '\u00AA',	'\u03B1', '\u00A9',	'\u2030', '\u011E',	'\u011B', '\u0148',	'\u0151',	
		  '\u03C0',	'\u20AC', '\u00A3',	'\u0024', '\u2190',	'\u2191', '\u2192',	'\u2193',	
		  '\u00BA', '\u00B9', '\u00B2',	'\u00B3', '\u00B1',	'\u0130', '\u0144',	'\u0171',	
		  '\u00B5',	'\u00BF', '\u00F7', '\u00B0', '\u00BC',	'\u00BD', '\u00BE',	'\u00A7',	
		  '\u00C1',	'\u00C0', '\u00C9',	'\u00C8', '\u00CD',	'\u00CC', '\u00D3',	'\u00D2',	
		  '\u00DA',	'\u00D9', '\u0158',	'\u010C', '\u0160',	'\u017D', '\u0110',	'\u013F',	
		  '\u00C2',	'\u00C4', '\u00CA',	'\u00CB', '\u00CE',	'\u00CF', '\u00D4',	'\u00D6',	
		  '\u00DB',	'\u00DC', '\u0159',	'\u010D', '\u0161',	'\u017E', '\u0111',	'\u0140',	
		  '\u00C3',	'\u00C5', '\u00C6', '\u0152', '\u0177',	'\u00DD', '\u00D5',	'\u00D8',	
		  '\u00DE',	'\u014A', '\u0154',	'\u0106', '\u015A',	'\u0179', '\u0166',	'\u00F0',	
		  '\u00E3',	'\u00E5', '\u00E6',	'\u0153', '\u0175',	'\u00FD', '\u00F5',	'\u00F8',	
		  '\u00FE',	'\u014B', '\u0155',	'\u0107', '\u015B',	'\u017A', '\u0167',	'\u0020',
	};

	
	public static char toChar(int code) {
		return charmap[code];
	}
}
