public class AUSTRALASIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "AUSTRALASIA__AUSTRALIA_AND_CHRISTMAS_ISLAND__108_E_TO_114_E__GDA94__MGA_ZONE_49__28349.SP":
                MyData.push("crs", "28349");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AUSTRALASIA__AUSTRALIA_AND_CHRISTMAS_ISLAND__WEST_OF_108_E__GDA94__MGA_ZONE_48__28348.SP":
                MyData.push("crs", "28348");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AUSTRALASIA__AUSTRALIA_AND_NORFOLK_ISLAND__162_E_TO_168_E__GDA94__MGA_ZONE_58__28358.SP":
                MyData.push("crs", "28358");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AUSTRALASIA__AUSTRALIA_AND_PNG__138_E_TO_144_E__AGD66__AMG_ZONE_54__20254.SP":
                MyData.push("crs", "20254");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AUSTRALASIA__AUSTRALIA_AND_PNG__144_E_TO_150_E__AGD66__AMG_ZONE_55__20255.SP":
                MyData.push("crs", "20255");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "AUSTRALASIA__AUSTRALIA_AND_PNG__150_E_TO_156_E__AGD66__AMG_ZONE_56__20256.SP":
                MyData.push("crs", "20256");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}