package db

import play.api.db.Database

trait DbImplicits {

  val db: Database
  val mainActorRef: akka.actor.ActorRef

  lazy implicit val usersDao: UsersDao = new UsersDao(db, mainActorRef)
  lazy implicit val projectsDao: ProjectsDao = new ProjectsDao(db, mainActorRef)
  lazy implicit val tokensDao: TokensDao = new TokensDao(db, mainActorRef)
  lazy implicit val syncsDao: SyncsDao = new SyncsDao(db)
  lazy implicit val projectBinariesDao: ProjectBinariesDao = new ProjectBinariesDao(db, mainActorRef)
  lazy implicit val projectLibrariesDao: ProjectLibrariesDao = new ProjectLibrariesDao(db, mainActorRef)
  lazy implicit val recommendationsDao: RecommendationsDao = new RecommendationsDao(db, mainActorRef)
  lazy implicit val librariesDao: LibrariesDao = new LibrariesDao(db, mainActorRef)
  lazy implicit val binariesDao: BinariesDao = new BinariesDao(db, mainActorRef)
  lazy implicit val resolversDao: ResolversDao = new ResolversDao(db, mainActorRef)
  lazy implicit val binaryRecommendationsDao: BinaryRecommendationsDao = new BinaryRecommendationsDao(db, mainActorRef)
  lazy implicit val libraryVersionsDao: LibraryVersionsDao = new LibraryVersionsDao(db, mainActorRef)
  lazy implicit val libraryRecommendationsDao: LibraryRecommendationsDao = new LibraryRecommendationsDao(db, mainActorRef)
  lazy implicit val itemsDao: ItemsDao = new ItemsDao(db, mainActorRef)
  lazy implicit val binaryVersionsDao: BinaryVersionsDao = new BinaryVersionsDao(db, mainActorRef)
  lazy implicit val subscriptionsDao: SubscriptionsDao = new SubscriptionsDao(db, mainActorRef)
  lazy implicit val organizationsDao: OrganizationsDao = new OrganizationsDao(db, mainActorRef)
  lazy implicit val userIdentifiersDao: UserIdentifiersDao = new UserIdentifiersDao(db)
  lazy implicit val githubUsersDao: GithubUsersDao = new GithubUsersDao(db, mainActorRef)
  lazy implicit val membershipsDao: MembershipsDao = new MembershipsDao(db)

}
