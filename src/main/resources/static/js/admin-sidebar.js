const adminSidebar =
    document.getElementById(
        "adminSidebar"
    );

const adminMenuButton =
    document.getElementById(
        "adminMenuButton"
    );


if (
    adminSidebar
    &&
    adminMenuButton
) {

    adminMenuButton.addEventListener(
        "click",
        function () {

            adminSidebar.classList
                .toggle("open");

        }
    );

}


/*
 * Close sidebar after selecting
 * navigation item on mobile.
 */
document
    .querySelectorAll(
        ".sidebar-link"
    )
    .forEach(link => {

        link.addEventListener(
            "click",
            function () {

                if (
                    window.innerWidth
                    <= 900
                ) {

                    adminSidebar
                        ?.classList
                        .remove("open");

                }

            }
        );

    });
