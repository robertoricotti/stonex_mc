public class LUXEMBOURGCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LUXEMBOURG__LUXEMBOURG_1930__GAUSS__2169.SP":
                MyData.push("crs", "2169");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}