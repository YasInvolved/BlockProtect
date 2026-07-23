package pl.yasinvolved.blockprotect.client;

public class ClientInspectState {
    private static boolean inspecting = false;

    public static boolean isInspecting() {
        return inspecting;
    }

    public static void setInspecting(boolean inspecting) {
        ClientInspectState.inspecting = inspecting;
    }
}
