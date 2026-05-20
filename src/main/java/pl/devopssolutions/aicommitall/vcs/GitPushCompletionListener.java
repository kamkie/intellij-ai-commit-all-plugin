/*
 * Copyright 2026 DevOps Solutions Kamil Kiewisz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pl.devopssolutions.aicommitall.vcs;

import git4idea.push.GitPushListener;
import git4idea.push.GitPushRepoResult;
import git4idea.repo.GitRepository;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Consumer;

final class GitPushCompletionListener implements GitPushListener {
    private final Consumer<GitRepository> completionHandler;

    GitPushCompletionListener(@NotNull Consumer<GitRepository> completionHandler) {
        this.completionHandler = Objects.requireNonNull(completionHandler);
    }

    @Override
    public void onCompleted(
            @NotNull GitRepository repository,
            @NotNull GitPushRepoResult pushResult
    ) {
        completionHandler.accept(repository);
    }
}
