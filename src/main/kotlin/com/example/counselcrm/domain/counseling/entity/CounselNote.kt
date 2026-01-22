package com.example.counselcrm.domain.counseling.entity

import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "counsel_notes")
class CounselNote(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counseling_id", nullable = false)
    val counseling: Counseling,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id", nullable = false)
    val counselor: Counselor,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String
) : BaseEntity() {

    fun updateContent(newContent: String) {
        this.content = newContent
    }
}
