package com.tarasovvp.cloudnativebackend.mapper

import com.tarasovvp.cloudnativebackend.DemoObjectWithOwner
import com.tarasovvp.cloudnativebackend.domain.models.DemoObject
import com.tarasovvp.cloudnativebackend.domain.models.Owner

fun DemoObjectWithOwner.toDemoObject() = DemoObject(
    demoObjectId = demoObjectId,
    name = name,
    htmlUrl = htmlUrl,
    description = description,
    owner = Owner(
        login = login,
        ownerId = ownerId,
        avatarUrl = avatarUrl,
        url = url,
    ),
)
