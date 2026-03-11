public class HEARD_ISLAND_AND_MCDONALD_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "HEARD_ISLAND_AND_MCDONALD_ISLANDS__66_E_TO_72_E__GDA94__MGA_ZONE_42__6733.SP":
                MyData.push("SECONDO_S_CRS", "6733");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "HEARD_ISLAND_AND_MCDONALD_ISLANDS__72_E_TO_78_E__GDA94__MGA_ZONE_43__6734.SP":
                MyData.push("SECONDO_S_CRS", "6734");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "HEARD_ISLAND_AND_MCDONALD_ISLANDS__EAST_OF_78_E__GDA94__MGA_ZONE_44__6735.SP":
                MyData.push("SECONDO_S_CRS", "6735");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
            case "HEARD_ISLAND_AND_MCDONALD_ISLANDS__WEST_OF_66_E__GDA94__MGA_ZONE_41__6732.SP":
                MyData.push("SECONDO_S_CRS", "6732");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}