public class NICARAGUACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "NICARAGUA__ONSHORE_NORTH_OF_12_48_N__OCOTEPEQUE_1935__NICARAGUA_NORTE__5461.SP":
                MyData.push("crs", "5461");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NICARAGUA__ONSHORE_SOUTH_OF_12_48_N__OCOTEPEQUE_1935__NICARAGUA_SUR__5462.SP":
                MyData.push("crs", "5462");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}