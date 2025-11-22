package com.example.resitrack.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    // Top-level destinations for the main navigation graph
    object Login : Screen("login_screen")
    object Registration : Screen("registration")
    object AdminMain : Screen("admin_main") // Container for Admin screens with bottom nav
    object ResidentMain : Screen("resident_main") // Container for Resident screens with bottom nav

    // Admin Screens that DO NOT have a bottom navigation bar
    object AdminComplaintDetails : Screen("admin_complaint_details/{complaintId}") {
        fun createRoute(complaintId: String) = "admin_complaint_details/$complaintId"
    }

    object AddEditResident : Screen("add_edit_resident") { // This is now the single base route.
        const val routeWithArgs = "add_edit_resident?residentId={residentId}"
        const val residentIdArg = "residentId"

        // Both helpers now build from the same base route.
        fun addRoute() = "add_edit_resident"
        fun editRoute(residentId: String) = "add_edit_resident?residentId=$residentId"
    }

    object AddEditNotice : Screen("add_edit_notice") {
        const val routeWithArgs = "add_edit_notice?noticeId={noticeId}"
        const val noticeIdArg= "noticeId"

        fun addRoute() = "add_edit_notice"
        fun editRoute(noticeId: String) = "add_edit_notice?noticeId=$noticeId"
    }
    object AddEditFacility : Screen("add_edit_facility") {
        const val routeWithArgs = "add_edit_facility?facilityId={facilityId}"
        const val facilityIdArg = "facilityId"

        fun addRoute() = "add_edit_facility"
        fun editRoute(facilityId: String) = "add_edit_facility?facilityId=$facilityId"
    }

    object AddEditMaintenanceCycle : Screen("add_edit_maintenance_cycle") {
        const val routeWithArgs = "add_edit_maintenance_cycle?cycleId={cycleId}"
        const val cycleIdArg = "cycleId"
        fun addRoute() = "add_edit_maintenance_cycle"
        fun editRoute(cycleId: String) = "add_edit_maintenance_cycle?cycleId=$cycleId"
    }

    object ManageMaintenanceFees : Screen("manage_maintenance_fees")

    // Resident Screens that DO NOT have a bottom navigation bar
    object FileNewComplaint : Screen("file_new_complaint")
    object BookFacility : Screen("book_facility")

    object AdminProfile : Screen("admin_profile")

    object EditMyProfile : Screen("edit_my_profile")
}

// Routes for screens WITHIN the Admin's Bottom Navigation
sealed class AdminBottomScreen(val route: String) {
    object Dashboard : AdminBottomScreen("admin_dashboard")
    object ManageResidents : AdminBottomScreen("manage_residents")
    object ManageNotices : AdminBottomScreen("manage_notices")
    object ViewAllComplaints : AdminBottomScreen("view_all_complaints")
    object ManageFacilities : AdminBottomScreen("manage_facilities")
}

// Routes for screens WITHIN the Resident's Bottom Navigation
sealed class ResidentBottomScreen(val route: String) {
    object Dashboard : ResidentBottomScreen("resident_dashboard")
    object ViewNotices : ResidentBottomScreen("view_notices")
    object MyComplaints : ResidentBottomScreen("my_complaints")
    object MyBookings : ResidentBottomScreen("my_bookings")
    object MyProfile : ResidentBottomScreen("my_profile")
}