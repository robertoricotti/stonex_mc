public class ALGERIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "ALGERIA__0_E_TO_6_E__NORD_SAHARA_1959__UTM_ZONE_31N__30731.SP":
                MyData.push("crs", "30731");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__31_30_N_TO_34_39_N__NORD_SAHARA_1959__SUD_ALGERIE__30792.SP":
                MyData.push("crs", "30792");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__32_N_TO_34_39_N__VOIROL_1875__SUD_ALGERIE__ANCIENNE__30492.SP":
                MyData.push("crs", "30492");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__32_N_TO_34_39_N__VOIROL_1879__SUD_ALGERIE__ANCIENNE__30494.SP":
                MyData.push("crs", "30494");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__6_W_TO_0_W__NORD_SAHARA_1959__UTM_ZONE_30N__30730.SP":
                MyData.push("crs", "30730");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__EAST_OF_6_E__NORD_SAHARA_1959__UTM_ZONE_32N__30732.SP":
                MyData.push("crs", "30732");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__NORTH_OF_34_39_N__NORD_SAHARA_1959__NORD_ALGERIE__30791.SP":
                MyData.push("crs", "30791");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__NORTH_OF_34_39_N__VOIROL_1875__NORD_ALGERIE__ANCIENNE__30491.SP":
                MyData.push("crs", "30491");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__NORTH_OF_34_39_N__VOIROL_1879__NORD_ALGERIE__ANCIENNE__30493.SP":
                MyData.push("crs", "30493");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "ALGERIA__WEST_OF_6_W__NORD_SAHARA_1959__UTM_ZONE_29N__30729.SP":
                MyData.push("crs", "30729");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}