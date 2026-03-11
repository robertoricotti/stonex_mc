public class VIETNAMCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "VIETNAM__103_5_E_TO_106_5_E_ONSHORE__VN_2000__TM_3_ZONE_482__6957.SP":
                MyData.push("crs", "6957");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__EAST_OF_106_5_E_ONSHORE__VN_2000__TM_3_ZONE_491__6958.SP":
                MyData.push("crs", "6958");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__EAST_OF_108_E_ONSHORE__HANOI_1972__GAUSS_KRUGER_ZONE_19__2045.SP":
                MyData.push("crs", "2045");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__EAST_OF_108_E_ONSHORE__INDIAN_1960__UTM_ZONE_49N__3149.SP":
                MyData.push("crs", "3149");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__EAST_OF_108_E_ONSHORE__VN_2000__UTM_ZONE_49N__3406.SP":
                MyData.push("crs", "3406");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__OFFSHORE_CUU_LONG_BASIN__INDIAN_1960__TM_106_NE__3176.SP":
                MyData.push("crs", "3176");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__OFFSHORE_CUU_LONG_BASIN__WGS_72BE__TM_106_NE__2094.SP":
                MyData.push("crs", "2094");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__ONSHORE_VUNG_TAU_AREA__HANOI_1972__GK_106_NE__2093.SP":
                MyData.push("crs", "2093");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__QUANG_NINH__DA_NANG__QUANG_NAM__BA_RIA_VUNG_TAU__DONG_NAI__LAM_DONG__VN_2000__TM_3_DA_NANG_ZONE__6959.SP":
                MyData.push("crs", "6959");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__WEST_OF_103_30_E_ONSHORE__VN_2000__TM_3_ZONE_481__6956.SP":
                MyData.push("crs", "6956");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__WEST_OF_108_E_ONSHORE__HANOI_1972__GAUSS_KRUGER_ZONE_18__2044.SP":
                MyData.push("crs", "2044");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "VIETNAM__WEST_OF_108_E_ONSHORE__VN_2000__UTM_ZONE_48N__3405.SP":
                MyData.push("crs", "3405");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}