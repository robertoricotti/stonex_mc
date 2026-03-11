public class LATVIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "LATVIA__LKS92__4948.SP":
                MyData.push("SECONDO_S_CRS", "4948");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "LATVIA__LKS92__GEOCENTRIC__4389.SP":
                MyData.push("SECONDO_S_CRS", "4389");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "LATVIA__LKS92__LATVIA_TM__3059.SP":
                MyData.push("SECONDO_S_CRS", "3059");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}