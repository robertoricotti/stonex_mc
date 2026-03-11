public class NETHERLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "RDNAPTRANS2018.SP":
                MyData.push("SECONDO_S_CRS", "28992");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NETHERLANDS__ONSHORE__AMERSFOORT__RD_NEW__28992.SP":
                MyData.push("SECONDO_S_CRS", "28992");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "NETHERLANDS__OFFSHORE__ED50__TM_5_NE__23095.SP":
                MyData.push("SECONDO_S_CRS", "23095");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
          
        }
    }
}