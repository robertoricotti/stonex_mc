public class GUATEMALACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "GUATEMALA__NORTH_OF_15_51_30_N__NAD27__GUATEMALA_NORTE__32061.SP":
                MyData.push("crs", "32061");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUATEMALA__NORTH_OF_15_51_30_N__OCOTEPEQUE_1935__GUATEMALA_NORTE__5458.SP":
                MyData.push("crs", "5458");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUATEMALA__NORTH_OF_15_51_30_N__OCOTEPEQUE_1935__GUATEMALA_NORTE__5559.SP":
                MyData.push("crs", "5559");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUATEMALA__SOUTH_OF_15_51_30_N__NAD27__GUATEMALA_SUR__32062.SP":
                MyData.push("crs", "32062");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "GUATEMALA__SOUTH_OF_15_51_30_N__OCOTEPEQUE_1935__GUATEMALA_SUR__5459.SP":
                MyData.push("crs", "5459");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}