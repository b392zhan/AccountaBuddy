package com.example.accountabuddy.Module

object Repo {
    private val groups = mutableMapOf<String, Group>()

    fun createGroup(groupName: String) {
        if (groupName.isBlank() || groups.containsKey(groupName)) return
        groups[groupName] = Group(groupName)
    }

    fun getGroup(groupName: String): Group? {
        return groups[groupName]
    }

    fun addMember(groupName: String, memberName: String) {
        val group = groups[groupName] ?: return
        if (!group.members.containsKey(memberName)) {
            group.members[memberName] = Member(name = memberName)
        }
    }

    fun logActivity(groupName: String, memberName: String, activity: String, duration: Int) {
        val group = groups[groupName] ?: return
        val member = group.members[memberName] ?: return

        val logEntry = LogEntry(activity, duration)
        member.activities.add(logEntry)
        updateStreak(member)
    }

    private fun updateStreak(member: Member) {
        val lastLogTime = member.activities.lastOrNull()?.timestamp ?: 0
        val currentTime = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000

        if (currentTime - lastLogTime <= oneDayMillis) {
            member.streak += 1
        } else {
            member.streak = 1
        }
    }

    fun getAllGroups(): List<Group> {
        return groups.values.toList()
    }
}
