public class AFRICACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "AFRICA__ANGOLA__CABINDA__AND_DR_CONGO__ZAIRE__COASTAL__MHAST__ONSHORE__UTM_ZONE_32S__3353.SP":
                MyData.push("SECONDO_S_CRS", "3353");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__ANGOLA__CABINDA__AND_DR_CONGO__ZAIRE__OFFSHORE__MALONGO_1987__UTM_ZONE_32S__25932.SP":
                MyData.push("SECONDO_S_CRS", "25932");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__ANGOLA__CABINDA__AND_DR_CONGO__ZAIRE__OFFSHORE__MHAST__OFFSHORE__UTM_ZONE_32S__3354.SP":
                MyData.push("SECONDO_S_CRS", "3354");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__BOTSWANA_AND_ZAMBIA__WEST_OF_24_E__ARC_1950__UTM_ZONE_34S__20934.SP":
                MyData.push("SECONDO_S_CRS", "20934");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__BOTSWANA__ZAMBIA_AND_ZIMBABWE__24_E_TO_30_E__ARC_1950__UTM_ZONE_35S__20935.SP":
                MyData.push("SECONDO_S_CRS", "20935");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__ERITREA__ETHIOPIA_AND_SUDAN__36_E_TO_42_E__ADINDAN__UTM_ZONE_37N__20137.SP":
                MyData.push("SECONDO_S_CRS", "20137");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__ETHIOPIA_AND_SUDAN__30_E_TO_36_E__ADINDAN__UTM_ZONE_36N__20136.SP":
                MyData.push("SECONDO_S_CRS", "20136");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__KENYA_AND_TANZANIA__SOUTH_OF_EQUATOR_AND_EAST_OF_36_E__ARC_1960__UTM_ZONE_37S__21037.SP":
                MyData.push("SECONDO_S_CRS", "21037");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__KENYA_AND_UGANDA__NORTH_OF_EQUATOR_AND_30_E_TO_36_E__ARC_1960__UTM_ZONE_36N__21096.SP":
                MyData.push("SECONDO_S_CRS", "21096");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__KENYA__TANZANIA_AND_UGANDA__SOUTH_OF_EQUATOR_AND_30_E_TO_36_E__ARC_1960__UTM_ZONE_36S__21036.SP":
                MyData.push("SECONDO_S_CRS", "21036");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__MALAWI__ZAMBIA_AND_ZIMBABWE__EAST_OF_30_E__ARC_1950__UTM_ZONE_36S__20936.SP":
                MyData.push("SECONDO_S_CRS", "20936");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__SOUTH_AFRICA__LESOTHO_AND_ESWATINI__HARTEBEESTHOEK94__4940.SP":
                MyData.push("SECONDO_S_CRS", "4940");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__SOUTH_SUDAN_AND_SUDAN__24_E_TO_30_E__ADINDAN__UTM_ZONE_35N__20135.SP":
                MyData.push("SECONDO_S_CRS", "20135");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "AFRICA__TANZANIA_AND_UGANDA__SOUTH_OF_EQUATOR_AND_WEST_OF_30_E__ARC_1960__UTM_ZONE_35S__21035.SP":
                MyData.push("SECONDO_S_CRS", "21035");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}