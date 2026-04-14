public class CAYMAN_ISLANDSCRSCases {
    public static void handleCRS(String input, MyData MyData, DataSaved DataSaved, Activity activity, Dialog dialog) {
        switch (input) {
            case "CAYMAN_ISLANDS__CAYMAN_ISLANDS_NATIONAL_GRID_2011__6141.SP":
                MyData.push("SECONDO_S_CRS", "6141");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__CAYMAN_ISLANDS_NATIONAL_GRID_2011__6391.SP":
                MyData.push("SECONDO_S_CRS", "6391");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__CIGD11__6133.SP":
                MyData.push("SECONDO_S_CRS", "6133");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__GRAND_CAYMAN__GRAND_CAYMAN_1959__UTM_ZONE_17N__3356.SP":
                MyData.push("SECONDO_S_CRS", "3356");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__GRAND_CAYMAN__GRAND_CAYMAN_NATIONAL_GRID_1959__6128.SP":
                MyData.push("SECONDO_S_CRS", "6128");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__LITTLE_CAYMAN_AND_CAYMAN_BRAC__LITTLE_CAYMAN_1961__UTM_ZONE_17N__3357.SP":
                MyData.push("SECONDO_S_CRS", "3357");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;

            case "CAYMAN_ISLANDS__LITTLE_CAYMAN_AND_CAYMAN_BRAC__SISTER_ISLANDS_NATIONAL_GRID_1961__6129.SP":
                MyData.push("SECONDO_S_CRS", "6129");
                DataSaved.SECONDO_S_CRS = MyData.get_String("SECONDO_S_CRS");
                activity.recreate();
                ReadProjectService.startCRS();
                dialog.dismiss();
                break;
        }
    }
}