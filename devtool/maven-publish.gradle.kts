//import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
//import com.vanniktech.maven.publish.SonatypeHost
//
//mavenPublishing {
//
//    configure(
//        AndroidSingleVariantLibrary(
//            variant = "release",
//            sourcesJar = true,
//            publishJavadocJar = false
//        )
//    )
//
//    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
//
//    signAllPublications()
//
//    coordinates(
//        property("GROUP").toString(),
//        property("POM_ARTIFACT_ID").toString(),
//        providers.gradleProperty("VERSION_NAME").get()
//    )
//
//    pom {
//
//        name.set(property("POM_NAME").toString())
//
//        description.set(
//            property("POM_DESCRIPTION").toString()
//        )
//
//        inceptionYear.set(
//            property("POM_INCEPTION_YEAR").toString()
//        )
//
//        url.set(
//            property("POM_URL").toString()
//        )
//
//        licenses {
//            license {
//                name.set(property("POM_LICENSE_NAME").toString())
//                url.set(property("POM_LICENSE_URL").toString())
//                distribution.set(property("POM_LICENSE_DIST").toString())
//            }
//        }
//
//        developers {
//
//            developer {
//
//                id.set(property("POM_DEVELOPER_ID").toString())
//
//                name.set(property("POM_DEVELOPER_NAME").toString())
//
//                email.set(property("POM_DEVELOPER_EMAIL").toString())
//            }
//        }
//
//        scm {
//
//            url.set(property("POM_SCM_URL").toString())
//
//            connection.set(property("POM_SCM_CONNECTION").toString())
//
//            developerConnection.set(
//                property("POM_SCM_DEV_CONNECTION").toString()
//            )
//        }
//    }
//}