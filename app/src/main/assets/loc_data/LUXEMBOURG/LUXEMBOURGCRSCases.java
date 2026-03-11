public class LUXEMBOURGCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LUXEMBOURG__LUXEMBOURG_1930__GAUSS__2169.SP":
                MyData.push("SECONDO_S_CRS", "2169");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}