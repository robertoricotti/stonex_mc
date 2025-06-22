package dxf;

import android.graphics.Color;

public class AutoCADColor {

    public static int getColor(String string) {
        int i = 0;
        int[] rgb = new int[]{0, 0, 0};
        try {
            i = Integer.parseInt(string);
            if (i >= 0){
                switch (i) {
                    case 0:
                        rgb = new int[]{0, 0, 0};
                        break;
                    case 1:
                        rgb = new int[]{255, 0, 0};
                        break;
                    case 2:
                        rgb = new int[]{255, 255, 0};
                        break;
                    case 3:
                        rgb = new int[]{0, 255, 0};
                        break;
                    case 4:
                        rgb = new int[]{0, 255, 255};
                        break;
                    case 5:
                        rgb = new int[]{0, 0, 255};
                        break;
                    case 6:
                        rgb = new int[]{255, 0, 255};
                        break;
                    case 7:
                        rgb = new int[]{0, 0, 0};
                        //rgb = new int[]{255, 255, 255};
                        break;
                    case 8:
                        rgb = new int[]{128, 128, 128};
                        break;
                    case 9:
                        rgb = new int[]{192, 192, 192};
                        break;
                    case 10:
                        rgb = new int[]{255, 0, 0};
                        break;
                    case 11:
                        rgb = new int[]{255, 127, 127};
                        break;
                    case 12:
                        rgb = new int[]{204, 0, 0};
                        break;
                    case 13:
                        rgb = new int[]{204, 102, 102};
                        break;
                    case 14:
                        rgb = new int[]{153, 0, 0};
                        break;
                    case 15:
                        rgb = new int[]{153, 76, 76};
                        break;
                    case 16:
                        rgb = new int[]{127, 0, 0};
                        break;
                    case 17:
                        rgb = new int[]{127, 63, 63};
                        break;
                    case 18:
                        rgb = new int[]{76, 0, 0};
                        break;
                    case 19:
                        rgb = new int[]{76, 38, 38};
                        break;
                    case 20:
                        rgb = new int[]{255, 63, 0};
                        break;
                    case 21:
                        rgb = new int[]{255, 159, 127};
                        break;
                    case 22:
                        rgb = new int[]{204, 51, 0};
                        break;
                    case 23:
                        rgb = new int[]{204, 127, 102};
                        break;
                    case 24:
                        rgb = new int[]{153, 38, 0};
                        break;
                    case 25:
                        rgb = new int[]{153, 95, 76};
                        break;
                    case 26:
                        rgb = new int[]{127, 31, 0};
                        break;
                    case 27:
                        rgb = new int[]{127, 79, 63};
                        break;
                    case 28:
                        rgb = new int[]{76, 19, 0};
                        break;
                    case 29:
                        rgb = new int[]{76, 47, 38};
                        break;
                    case 30:
                        rgb = new int[]{255, 127, 0};
                        break;
                    case 31:
                        rgb = new int[]{255, 191, 127};
                        break;
                    case 32:
                        rgb = new int[]{204, 102, 0};
                        break;
                    case 33:
                        rgb = new int[]{204, 153, 102};
                        break;
                    case 34:
                        rgb = new int[]{153, 76, 0};
                        break;
                    case 35:
                        rgb = new int[]{153, 114, 76};
                        break;
                    case 36:
                        rgb = new int[]{127, 63, 0};
                        break;
                    case 37:
                        rgb = new int[]{127, 95, 63};
                        break;
                    case 38:
                        rgb = new int[]{76, 38, 0};
                        break;
                    case 39:
                        rgb = new int[]{76, 57, 38};
                        break;
                    case 40:
                        rgb = new int[]{255, 191, 0};
                        break;
                    case 41:
                        rgb = new int[]{255, 223, 127};
                        break;
                    case 42:
                        rgb = new int[]{204, 153, 0};
                        break;
                    case 43:
                        rgb = new int[]{204, 178, 102};
                        break;
                    case 44:
                        rgb = new int[]{153, 114, 0};
                        break;
                    case 45:
                        rgb = new int[]{153, 133, 76};
                        break;
                    case 46:
                        rgb = new int[]{127, 95, 0};
                        break;
                    case 47:
                        rgb = new int[]{127, 111, 63};
                        break;
                    case 48:
                        rgb = new int[]{76, 57, 0};
                        break;
                    case 49:
                        rgb = new int[]{76, 66, 38};
                        break;
                    case 50:
                        rgb = new int[]{255, 255, 0};
                        break;
                    case 51:
                        rgb = new int[]{255, 255, 127};
                        break;
                    case 52:
                        rgb = new int[]{204, 204, 0};
                        break;
                    case 53:
                        rgb = new int[]{204, 204, 102};
                        break;
                    case 54:
                        rgb = new int[]{153, 153, 0};
                        break;
                    case 55:
                        rgb = new int[]{153, 153, 76};
                        break;
                    case 56:
                        rgb = new int[]{127, 127, 0};
                        break;
                    case 57:
                        rgb = new int[]{127, 127, 63};
                        break;
                    case 58:
                        rgb = new int[]{76, 76, 0};
                        break;
                    case 59:
                        rgb = new int[]{76, 76, 38};
                        break;
                    case 60:
                        rgb = new int[]{191, 255, 0};
                        break;
                    case 61:
                        rgb = new int[]{223, 255, 127};
                        break;
                    case 62:
                        rgb = new int[]{153, 204, 0};
                        break;
                    case 63:
                        rgb = new int[]{178, 204, 102};
                        break;
                    case 64:
                        rgb = new int[]{114, 153, 0};
                        break;
                    case 65:
                        rgb = new int[]{133, 153, 76};
                        break;
                    case 66:
                        rgb = new int[]{95, 127, 0};
                        break;
                    case 67:
                        rgb = new int[]{111, 127, 63};
                        break;
                    case 68:
                        rgb = new int[]{57, 76, 0};
                        break;
                    case 69:
                        rgb = new int[]{66, 76, 38};
                        break;
                    case 70:
                        rgb = new int[]{127, 255, 0};
                        break;
                    case 71:
                        rgb = new int[]{191, 255, 127};
                        break;
                    case 72:
                        rgb = new int[]{102, 204, 0};
                        break;
                    case 73:
                        rgb = new int[]{153, 204, 102};
                        break;
                    case 74:
                        rgb = new int[]{76, 153, 0};
                        break;
                    case 75:
                        rgb = new int[]{114, 153, 76};
                        break;
                    case 76:
                        rgb = new int[]{63, 127, 0};
                        break;
                    case 77:
                        rgb = new int[]{95, 127, 63};
                        break;
                    case 78:
                        rgb = new int[]{38, 76, 0};
                        break;
                    case 79:
                        rgb = new int[]{57, 76, 38};
                        break;
                    case 80:
                        rgb = new int[]{63, 255, 0};
                        break;
                    case 81:
                        rgb = new int[]{159, 255, 127};
                        break;
                    case 82:
                        rgb = new int[]{51, 204, 0};
                        break;
                    case 83:
                        rgb = new int[]{127, 204, 102};
                        break;
                    case 84:
                        rgb = new int[]{38, 153, 0};
                        break;
                    case 85:
                        rgb = new int[]{95, 153, 76};
                        break;
                    case 86:
                        rgb = new int[]{31, 127, 0};
                        break;
                    case 87:
                        rgb = new int[]{79, 127, 63};
                        break;
                    case 88:
                        rgb = new int[]{19, 76, 0};
                        break;
                    case 89:
                        rgb = new int[]{47, 76, 38};
                        break;
                    case 90:
                        rgb = new int[]{0, 255, 0};
                        break;
                    case 91:
                        rgb = new int[]{127, 255, 127};
                        break;
                    case 92:
                        rgb = new int[]{0, 204, 0};
                        break;
                    case 93:
                        rgb = new int[]{102, 204, 102};
                        break;
                    case 94:
                        rgb = new int[]{0, 153, 0};
                        break;
                    case 95:
                        rgb = new int[]{76, 153, 76};
                        break;
                    case 96:
                        rgb = new int[]{0, 127, 0};
                        break;
                    case 97:
                        rgb = new int[]{63, 127, 63};
                        break;
                    case 98:
                        rgb = new int[]{0, 76, 0};
                        break;
                    case 99:
                        rgb = new int[]{38, 76, 38};
                        break;
                    case 100:
                        rgb = new int[]{0, 255, 63};
                        break;
                    case 101:
                        rgb = new int[]{127, 255, 159};
                        break;
                    case 102:
                        rgb = new int[]{0, 204, 51};
                        break;
                    case 103:
                        rgb = new int[]{102, 204, 127};
                        break;
                    case 104:
                        rgb = new int[]{0, 153, 38};
                        break;
                    case 105:
                        rgb = new int[]{76, 153, 95};
                        break;
                    case 106:
                        rgb = new int[]{0, 127, 31};
                        break;
                    case 107:
                        rgb = new int[]{63, 127, 79};
                        break;
                    case 108:
                        rgb = new int[]{0, 76, 19};
                        break;
                    case 109:
                        rgb = new int[]{38, 76, 47};
                        break;
                    case 110:
                        rgb = new int[]{0, 255, 127};
                        break;
                    case 111:
                        rgb = new int[]{127, 255, 191};
                        break;
                    case 112:
                        rgb = new int[]{0, 204, 102};
                        break;
                    case 113:
                        rgb = new int[]{102, 204, 153};
                        break;
                    case 114:
                        rgb = new int[]{0, 153, 76};
                        break;
                    case 115:
                        rgb = new int[]{76, 153, 114};
                        break;
                    case 116:
                        rgb = new int[]{0, 127, 63};
                        break;
                    case 117:
                        rgb = new int[]{63, 127, 95};
                        break;
                    case 118:
                        rgb = new int[]{0, 76, 38};
                        break;
                    case 119:
                        rgb = new int[]{38, 76, 57};
                        break;
                    case 120:
                        rgb = new int[]{0, 255, 191};
                        break;
                    case 121:
                        rgb = new int[]{127, 255, 223};
                        break;
                    case 122:
                        rgb = new int[]{0, 204, 153};
                        break;
                    case 123:
                        rgb = new int[]{102, 204, 178};
                        break;
                    case 124:
                        rgb = new int[]{0, 153, 114};
                        break;
                    case 125:
                        rgb = new int[]{76, 153, 133};
                        break;
                    case 126:
                        rgb = new int[]{0, 127, 95};
                        break;
                    case 127:
                        rgb = new int[]{63, 127, 111};
                        break;
                    case 128:
                        rgb = new int[]{0, 76, 57};
                        break;
                    case 129:
                        rgb = new int[]{38, 76, 66};
                        break;
                    case 130:
                        rgb = new int[]{0, 255, 255};
                        break;
                    case 131:
                        rgb = new int[]{127, 255, 255};
                        break;
                    case 132:
                        rgb = new int[]{0, 204, 204};
                        break;
                    case 133:
                        rgb = new int[]{102, 204, 204};
                        break;
                    case 134:
                        rgb = new int[]{0, 153, 153};
                        break;
                    case 135:
                        rgb = new int[]{76, 153, 153};
                        break;
                    case 136:
                        rgb = new int[]{0, 127, 127};
                        break;
                    case 137:
                        rgb = new int[]{63, 127, 127};
                        break;
                    case 138:
                        rgb = new int[]{0, 76, 76};
                        break;
                    case 139:
                        rgb = new int[]{38, 76, 76};
                        break;
                    case 140:
                        rgb = new int[]{0, 191, 255};
                        break;
                    case 141:
                        rgb = new int[]{127, 223, 255};
                        break;
                    case 142:
                        rgb = new int[]{0, 153, 204};
                        break;
                    case 143:
                        rgb = new int[]{102, 178, 204};
                        break;
                    case 144:
                        rgb = new int[]{0, 114, 153};
                        break;
                    case 145:
                        rgb = new int[]{76, 133, 153};
                        break;
                    case 146:
                        rgb = new int[]{0, 95, 127};
                        break;
                    case 147:
                        rgb = new int[]{63, 111, 127};
                        break;
                    case 148:
                        rgb = new int[]{0, 57, 76};
                        break;
                    case 149:
                        rgb = new int[]{38, 66, 76};
                        break;
                    case 150:
                        rgb = new int[]{0, 127, 255};
                        break;
                    case 151:
                        rgb = new int[]{127, 191, 255};
                        break;
                    case 152:
                        rgb = new int[]{0, 102, 204};
                        break;
                    case 153:
                        rgb = new int[]{102, 153, 204};
                        break;
                    case 154:
                        rgb = new int[]{0, 76, 153};
                        break;
                    case 155:
                        rgb = new int[]{76, 114, 153};
                        break;
                    case 156:
                        rgb = new int[]{0, 63, 127};
                        break;
                    case 157:
                        rgb = new int[]{63, 95, 127};
                        break;
                    case 158:
                        rgb = new int[]{0, 38, 76};
                        break;
                    case 159:
                        rgb = new int[]{38, 57, 76};
                        break;
                    case 160:
                        rgb = new int[]{0, 63, 255};
                        break;
                    case 161:
                        rgb = new int[]{127, 159, 255};
                        break;
                    case 162:
                        rgb = new int[]{0, 51, 204};
                        break;
                    case 163:
                        rgb = new int[]{102, 127, 204};
                        break;
                    case 164:
                        rgb = new int[]{0, 38, 153};
                        break;
                    case 165:
                        rgb = new int[]{76, 95, 153};
                        break;
                    case 166:
                        rgb = new int[]{0, 31, 127};
                        break;
                    case 167:
                        rgb = new int[]{63, 79, 127};
                        break;
                    case 168:
                        rgb = new int[]{0, 19, 76};
                        break;
                    case 169:
                        rgb = new int[]{38, 47, 76};
                        break;
                    case 170:
                        rgb = new int[]{0, 0, 255};
                        break;
                    case 171:
                        rgb = new int[]{127, 127, 255};
                        break;
                    case 172:
                        rgb = new int[]{0, 0, 204};
                        break;
                    case 173:
                        rgb = new int[]{102, 102, 204};
                        break;
                    case 174:
                        rgb = new int[]{0, 0, 153};
                        break;
                    case 175:
                        rgb = new int[]{76, 76, 153};
                        break;
                    case 176:
                        rgb = new int[]{0, 0, 127};
                        break;
                    case 177:
                        rgb = new int[]{63, 63, 127};
                        break;
                    case 178:
                        rgb = new int[]{0, 0, 76};
                        break;
                    case 179:
                        rgb = new int[]{38, 38, 76};
                        break;
                    case 180:
                        rgb = new int[]{63, 0, 255};
                        break;
                    case 181:
                        rgb = new int[]{159, 127, 255};
                        break;
                    case 182:
                        rgb = new int[]{51, 0, 204};
                        break;
                    case 183:
                        rgb = new int[]{127, 102, 204};
                        break;
                    case 184:
                        rgb = new int[]{38, 0, 153};
                        break;
                    case 185:
                        rgb = new int[]{95, 76, 153};
                        break;
                    case 186:
                        rgb = new int[]{31, 0, 127};
                        break;
                    case 187:
                        rgb = new int[]{79, 63, 127};
                        break;
                    case 188:
                        rgb = new int[]{19, 0, 76};
                        break;
                    case 189:
                        rgb = new int[]{47, 38, 76};
                        break;
                    case 190:
                        rgb = new int[]{127, 0, 255};
                        break;
                    case 191:
                        rgb = new int[]{191, 127, 255};
                        break;
                    case 192:
                        rgb = new int[]{102, 0, 204};
                        break;
                    case 193:
                        rgb = new int[]{153, 102, 204};
                        break;
                    case 194:
                        rgb = new int[]{76, 0, 153};
                        break;
                    case 195:
                        rgb = new int[]{114, 76, 153};
                        break;
                    case 196:
                        rgb = new int[]{63, 0, 127};
                        break;
                    case 197:
                        rgb = new int[]{95, 63, 127};
                        break;
                    case 198:
                        rgb = new int[]{38, 0, 76};
                        break;
                    case 199:
                        rgb = new int[]{57, 38, 76};
                        break;
                    case 200:
                        rgb = new int[]{191, 0, 255};
                        break;
                    case 201:
                        rgb = new int[]{223, 127, 255};
                        break;
                    case 202:
                        rgb = new int[]{153, 0, 204};
                        break;
                    case 203:
                        rgb = new int[]{178, 102, 204};
                        break;
                    case 204:
                        rgb = new int[]{114, 0, 153};
                        break;
                    case 205:
                        rgb = new int[]{133, 76, 153};
                        break;
                    case 206:
                        rgb = new int[]{95, 0, 127};
                        break;
                    case 207:
                        rgb = new int[]{111, 63, 127};
                        break;
                    case 208:
                        rgb = new int[]{57, 0, 76};
                        break;
                    case 209:
                        rgb = new int[]{66, 38, 76};
                        break;
                    case 210:
                        rgb = new int[]{255, 0, 255};
                        break;
                    case 211:
                        rgb = new int[]{255, 127, 255};
                        break;
                    case 212:
                        rgb = new int[]{204, 0, 204};
                        break;
                    case 213:
                        rgb = new int[]{204, 102, 204};
                        break;
                    case 214:
                        rgb = new int[]{153, 0, 153};
                        break;
                    case 215:
                        rgb = new int[]{153, 76, 153};
                        break;
                    case 216:
                        rgb = new int[]{127, 0, 127};
                        break;
                    case 217:
                        rgb = new int[]{127, 63, 127};
                        break;
                    case 218:
                        rgb = new int[]{76, 0, 76};
                        break;
                    case 219:
                        rgb = new int[]{76, 38, 76};
                        break;
                    case 220:
                        rgb = new int[]{255, 0, 191};
                        break;
                    case 221:
                        rgb = new int[]{255, 127, 223};
                        break;
                    case 222:
                        rgb = new int[]{204, 0, 153};
                        break;
                    case 223:
                        rgb = new int[]{204, 102, 178};
                        break;
                    case 224:
                        rgb = new int[]{153, 0, 114};
                        break;
                    case 225:
                        rgb = new int[]{153, 76, 133};
                        break;
                    case 226:
                        rgb = new int[]{127, 0, 95};
                        break;
                    case 227:
                        rgb = new int[]{127, 63, 111};
                        break;
                    case 228:
                        rgb = new int[]{76, 0, 57};
                        break;
                    case 229:
                        rgb = new int[]{76, 38, 66};
                        break;
                    case 230:
                        rgb = new int[]{255, 0, 127};
                        break;
                    case 231:
                        rgb = new int[]{255, 127, 191};
                        break;
                    case 232:
                        rgb = new int[]{204, 0, 102};
                        break;
                    case 233:
                        rgb = new int[]{204, 102, 153};
                        break;
                    case 234:
                        rgb = new int[]{153, 0, 76};
                        break;
                    case 235:
                        rgb = new int[]{153, 76, 114};
                        break;
                    case 236:
                        rgb = new int[]{127, 0, 63};
                        break;
                    case 237:
                        rgb = new int[]{127, 63, 95};
                        break;
                    case 238:
                        rgb = new int[]{76, 0, 38};
                        break;
                    case 239:
                        rgb = new int[]{76, 38, 57};
                        break;
                    case 240:
                        rgb = new int[]{255, 0, 63};
                        break;
                    case 241:
                        rgb = new int[]{255, 127, 159};
                        break;
                    case 242:
                        rgb = new int[]{204, 0, 51};
                        break;
                    case 243:
                        rgb = new int[]{204, 102, 127};
                        break;
                    case 244:
                        rgb = new int[]{153, 0, 38};
                        break;
                    case 245:
                        rgb = new int[]{153, 76, 95};
                        break;
                    case 246:
                        rgb = new int[]{127, 0, 31};
                        break;
                    case 247:
                        rgb = new int[]{127, 63, 79};
                        break;
                    case 248:
                        rgb = new int[]{76, 0, 19};
                        break;
                    case 249:
                        rgb = new int[]{76, 38, 47};
                        break;
                    case 250:
                        rgb = new int[]{223, 223, 223};
                        break;
                    case 251:
                        rgb = new int[]{239, 239, 239};
                        break;
                    case 252:
                        rgb = new int[]{247, 247, 247};
                        break;
                    case 253:
                        rgb = new int[]{251, 251, 251};
                        break;
                    case 254:
                        rgb = new int[]{253, 253, 253};
                        break;
                    case 255:
                        rgb = new int[]{255, 255, 255};
                        break;
                    default:
                        rgb = new int[]{0, 0, 0};
                        break;
                }
        }else {
                rgb=new int[]{-1,-1,-1};
            }
        } catch (NumberFormatException e) {
            rgb = new int[]{0, 0, 0};
        }

        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }
}
