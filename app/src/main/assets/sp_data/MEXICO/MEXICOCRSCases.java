public class MEXICOCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "MEXICO__102_W_TO_96_W__MEXICO_ITRF2008__UTM_ZONE_14N__6369.SP":
                MyData.push("crs", "6369");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__102_W_TO_96_W__MEXICO_ITRF92__UTM_ZONE_14N__4487.SP":
                MyData.push("crs", "4487");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__108_W_TO_102_W__MEXICO_ITRF2008__UTM_ZONE_13N__6368.SP":
                MyData.push("crs", "6368");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__108_W_TO_102_W__MEXICO_ITRF92__UTM_ZONE_13N__4486.SP":
                MyData.push("crs", "4486");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__114_W_TO_108_W__MEXICO_ITRF2008__UTM_ZONE_12N__6367.SP":
                MyData.push("crs", "6367");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__114_W_TO_108_W__MEXICO_ITRF92__UTM_ZONE_12N__4485.SP":
                MyData.push("crs", "4485");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__96_W_TO_90_W__MEXICO_ITRF2008__UTM_ZONE_15N__6370.SP":
                MyData.push("crs", "6370");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__96_W_TO_90_W__MEXICO_ITRF92__UTM_ZONE_15N__4488.SP":
                MyData.push("crs", "4488");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__EAST_OF_90_W__MEXICO_ITRF2008__UTM_ZONE_16N__6371.SP":
                MyData.push("crs", "6371");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__EAST_OF_90_W__MEXICO_ITRF92__UTM_ZONE_16N__4489.SP":
                MyData.push("crs", "4489");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__MEXICO_ITRF2008__6363.SP":
                MyData.push("crs", "6363");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__MEXICO_ITRF2008__LCC__6372.SP":
                MyData.push("crs", "6372");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__MEXICO_ITRF92__4481.SP":
                MyData.push("crs", "4481");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__MEXICO_ITRF92__LCC__6362.SP":
                MyData.push("crs", "6362");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__WEST_OF_114_W__MEXICO_ITRF2008__UTM_ZONE_11N__6366.SP":
                MyData.push("crs", "6366");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "MEXICO__WEST_OF_114_W__MEXICO_ITRF92__UTM_ZONE_11N__4484.SP":
                MyData.push("crs", "4484");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}