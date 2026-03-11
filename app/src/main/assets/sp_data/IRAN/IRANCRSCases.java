public class IRANCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "IRAN__48_E_TO_54_E__ED50_ED77__UTM_ZONE_39N__2059.SP":
                MyData.push("crs", "2059");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__54_E_TO_60_E__ED50_ED77__UTM_ZONE_40N__2060.SP":
                MyData.push("crs", "2060");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__EAST_OF_60_E_ONSHORE__ED50_ED77__UTM_ZONE_41N__2061.SP":
                MyData.push("crs", "2061");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__FD58__FD58__IRAQ_ZONE__3200.SP":
                MyData.push("crs", "3200");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__KANGAN_DISTRICT__NAKHL_E_GHANEM__UTM_ZONE_39N__3307.SP":
                MyData.push("crs", "3307");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__TAHERI_REFINERY__RASSADIRAN__NAKHL_E_TAQI__2057.SP":
                MyData.push("crs", "2057");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "IRAN__WEST_OF_48_E__ED50_ED77__UTM_ZONE_38N__2058.SP":
                MyData.push("crs", "2058");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}