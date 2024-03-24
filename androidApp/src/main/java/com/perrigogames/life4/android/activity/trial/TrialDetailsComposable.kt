package com.perrigogames.life4.android.activity.trial

//@Composable
//fun TrialDetails(
//    trial: Trial,
//    viewModel: TrialDetailsViewModel = viewModel(
//        factory = createViewModelFactory { TrialDetailsViewModel(trial) }
//    ),
//) {
//    Column {
//        TrialDetailsHeader(
//            trial = trial,
//            modifier = Modifier.fillMaxWidth()
//        )
//        TrialDetailsControls(
//            trial = trial,
//            viewModel = viewModel,
//            modifier = Modifier.fillMaxWidth(),
//        )
//        LazyColumn {
//
//        }
//    }
//}
//
//@Composable
//fun TrialDetailsHeader(
//    trial: Trial,
//    modifier: Modifier = Modifier,
//) {
//    Row(modifier) {
//        // Button previous
////        Image()
//        // Button next
//    }
//}

//@Composable
//fun TrialDetailsControls(
//    trial: Trial,
//    viewModel: TrialDetailsViewModel = viewModel(
//        factory = createViewModelFactory { TrialDetailsViewModel(trial) }
//    ),
//    modifier: Modifier = Modifier,
//) {
//    val targetRankView: TargetRankView by viewModel.targetRankView.collectAsState()
//    var dropdownExpanded by remember { mutableStateOf(false) }
//    val filesystemChecked by viewModel.filesystemChecked.collectAsState()
//    val exProgress by viewModel.exProgress.collectAsState()
//    val songViews by viewModel.songViews.collectAsState()
//
//    Column(modifier) {
//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            Row {
//                RankImage(
//                    rank = targetRankView.rank.parent,
//                    size = 32.dp,
//                )
//                DropdownMenu(
//                    expanded = dropdownExpanded,
//                    onDismissRequest = { dropdownExpanded = false },
//                ) {
//                    targetRankView.availableRanks.forEach { rank ->
//                        DropdownMenuItem(
//                            onClick = {
//                                viewModel.setTargetRank(rank)
//                                dropdownExpanded = false
//                            },
//                            text = {
//                                Text(text = stringResource(rank.nameRes))
//                            },
//                        )
//                    }
//                }
//            }
//            Row {
//                Image(
//                    painter = painterResource(R.drawable.ic_photo_camera),
//                    contentDescription = stringResource(R.string.content_desc_photos_from_camera),
//                    colorFilter = ColorFilter.tint(
//                        color = MaterialTheme.colorScheme.onBackground,
//                    ),
//                )
//                Switch(
//                    checked = filesystemChecked,
//                    onCheckedChange = viewModel::setFilesystemChecked,
//                )
//                Image(
//                    painter = painterResource(R.drawable.ic_file_folder),
//                    contentDescription = stringResource(R.string.content_desc_photos_from_filesystem),
//                    colorFilter = ColorFilter.tint(
//                        color = MaterialTheme.colorScheme.onBackground,
//                    ),
//                )
//            }
//        }
//        Text(
//            text = targetRankView.goalText
//        )
//        RunningEXScore(
//            progress = exProgress,
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentHeight(),
//        )
//        TrialSongList(songViews)
//    }
//}

//@Composable
//fun TrialSongList(
//    songs: List<TrialSongView>
//) {
//    LazyColumn {
//        items(songs) { song ->
//            TrialSongCard(song = song)
//        }
//    }
//}
//
//@Composable
//fun TrialSongCard(
//    song: TrialSongView,
//    modifier: Modifier = Modifier,
//) {
//    Card(modifier = modifier) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            if (song.jacketUrl != null) {
//                AsyncImage(
//                    model = song.jacketUrl,
//                    contentDescription = "jacket for ${song.title}"
//                )
//            } else {
//                Image(
//                    painter = painterResource(R.drawable.trial_default),
//                    contentDescription = "jacket for ${song.title}"
//                )
//            }
//            Column(
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Text(
//                        text = song.title
//                    )
//
//                    Image(
//                        painter = painterResource(R.drawable.ic_photo_camera),
//                        contentDescription = stringResource(
//                            if (song.hasResult) R.string.content_desc_photo_taken
//                            else R.string.content_desc_photo_not_taken
//                        ),
//                        alpha = if (song.hasResult) 1f else 0f,
//                        colorFilter = ColorFilter.lighting(
//                            add = MaterialTheme.colorScheme.onTertiaryContainer,
//                            multiply = Color.White,
//                        ),
//                        modifier = Modifier.aspectRatio(1f)
//                    )
//                }
//                Row {
//                    Column {
//                        DifficultyText(
//                            difficultyClass = song.difficultyClass,
//                            difficultyNumber = song.difficultyNumber,
//                        )
//
//                    }
//                    if (song.hasResult) {
//                        Text(
//                            text = song.resultText,
//                            fontWeight = if (song.resultBold) FontWeight.Bold else FontWeight.Normal,
//                            color = MaterialTheme.colorScheme.onTertiaryContainer,
//                        )
//                    }
//                }
//            }
//        }
//    }
//}
//
//@Composable
//@Preview(widthDp = 240)
//fun TrialSongCardPreview() {
//    LIFE4Theme {
//        TrialSongCard(
//            TrialSongView(
//                song = Song(
//                    name = "AWESOME SONG NAME",
//                    difficultyNumber = 14,
//                    difficultyClass = DifficultyClass.EXPERT,
//                    ex = 10000,
//                    url = "https://life4-mobile.s3.us-west-1.amazonaws.com/images/jackets/songs/Dopamine-jacket.webp"
//                ),
//                result = null
//            )
//        )
//    }
//}