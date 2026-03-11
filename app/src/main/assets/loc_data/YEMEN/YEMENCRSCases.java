public class YEMENCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "YEMEN__42_E_TO_48_E__YEMEN_NGN96__UTM_ZONE_38N__2089.SP":
                MyData.push("SECONSO_S_CRS", "2089");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__48_E_TO_54_E__YEMEN_NGN96__UTM_ZONE_39N__2090.SP":
                MyData.push("SECONSO_S_CRS", "2090");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__EAST_OF_54_E__YEMEN_NGN96__UTM_ZONE_40N__5837.SP":
                MyData.push("SECONSO_S_CRS", "5837");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__SOUTH_YEMEN__MAINLAND_EAST_OF_48_E__SOUTH_YEMEN__GAUSS_KRUGER_ZONE_9__2092.SP":
                MyData.push("SECONSO_S_CRS", "2092");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__SOUTH_YEMEN__MAINLAND_EAST_OF_48_E__SOUTH_YEMEN__GAUSS_KRUGER_ZONE_9__2396.SP":
                MyData.push("SECONSO_S_CRS", "2396");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__SOUTH_YEMEN__MAINLAND_WEST_OF_48_E__SOUTH_YEMEN__GAUSS_KRUGER_ZONE_8__2091.SP":
                MyData.push("SECONSO_S_CRS", "2091");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__SOUTH_YEMEN__MAINLAND_WEST_OF_48_E__SOUTH_YEMEN__GAUSS_KRUGER_ZONE_8__2395.SP":
                MyData.push("SECONSO_S_CRS", "2395");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__WEST_OF_42_E__YEMEN_NGN96__UTM_ZONE_37N__5836.SP":
                MyData.push("SECONSO_S_CRS", "5836");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__YEMEN_NGN96__4980.SP":
                MyData.push("SECONSO_S_CRS", "4980");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "YEMEN__YEMEN_NGN96__GEOCENTRIC__4380.SP":
                MyData.push("SECONSO_S_CRS", "4380");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONSO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}