public class NETHERLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "RDNAPTRANS2018.SP":
                MyData.push("crs", "28992");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NETHERLANDS__ONSHORE__AMERSFOORT__RD_NEW__28992.SP":
                MyData.push("crs", "28992");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NETHERLANDS__OFFSHORE__ED50__TM_5_NE__23095.SP":
                MyData.push("crs", "23095");
                DataSaved.S_CRS = MyData.get_String("crs");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
          
        }
    }
}