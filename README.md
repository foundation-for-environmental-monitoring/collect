# ffem Collect
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/collect)
[![codecov.io](https://codecov.io/github/opendatakit/collect/branch/master/graph/badge.svg)](https://codecov.io/github/opendatakit/collect)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

ffem Collect is an Android app for filling out forms. It is integrated with ffem Caddisfly.

ffem Collect is a fork of the Open Data Kit (ODK) Collect project, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

ffem Collect renders forms that are compliant with the [ODK XForms standard](http://opendatakit.github.io/xforms-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/opendatakit/javarosa) which Collect includes as a dependency.

## Table of Contents
* [Learn more about ODK Collect](#learn-more-about-odk-collect)
* [Setting up your development environment](#setting-up-your-development-environment)

## Learn more about ODK Collect
* ODK website: [https://opendatakit.org](https://opendatakit.org)
* ODK Collect usage documentation: [https://docs.opendatakit.org/collect-intro/](https://docs.opendatakit.org/collect-intro/)
* ODK forum: [https://forum.opendatakit.org](https://forum.opendatakit.org)
* ODK developer Slack chat: [http://slack.opendatakit.org](http://slack.opendatakit.org) 
* ODK developer Slack archive: [https://opendatakit.slackarchive.io](https://opendatakit.slackarchive.io) 

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

 If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`. 

1. Open the project in the folder of your clone from Android Studio. To run the project, click on the green arrow at the top of the screen. The emulator is very slow so we generally recommend using a physical device when possible.
