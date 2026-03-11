public class SLOVENIACRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "SLOVENIA__MGI_1901__SLOVENE_NATIONAL_GRID__3912.SP":
                MyData.push("SECONDO_S_CRS", "3912");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SLOVENIA__MGI_1901__SLOVENIA_GRID__3911.SP":
                MyData.push("SECONDO_S_CRS", "3911");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SLOVENIA__MGI__SLOVENE_NATIONAL_GRID__3787.SP":
                MyData.push("SECONDO_S_CRS", "3787");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SLOVENIA__MGI__SLOVENIA_GRID__2170.SP":
                MyData.push("SECONDO_S_CRS", "2170");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SLOVENIA__SLOVENIA_1996__4882.SP":
                MyData.push("SECONDO_S_CRS", "4882");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "SLOVENIA__SLOVENIA_1996__SLOVENE_NATIONAL_GRID__3794.SP":
                MyData.push("SECONDO_S_CRS", "3794");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}