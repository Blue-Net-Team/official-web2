{{/*
通用 chart 的公共模板：所有服务共享同一套 Deployment/StatefulSet/CronJob/Service 模板，
差异全部通过 values 表达，避免每个服务复制一套 manifest。
*/}}

{{/* 资源名前缀：默认取 release 名（即每服务一个 release） */}}
{{- define "bluenet.name" -}}
{{- default .Release.Name .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "bluenet.labels" -}}
app.kubernetes.io/name: {{ include "bluenet.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/part-of: bluenet
{{- with .Values.extraLabels }}
{{ toYaml . }}
{{- end }}
{{- end -}}

{{- define "bluenet.selectorLabels" -}}
app.kubernetes.io/name: {{ include "bluenet.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{- define "bluenet.image" -}}
{{- $repo := .Values.image.repository -}}
{{- $tag := .Values.image.tag | toString -}}
{{- if .Values.image.digest -}}
{{- if $tag -}}
{{- printf "%s:%s@%s" $repo $tag .Values.image.digest -}}
{{- else -}}
{{- printf "%s@%s" $repo .Values.image.digest -}}
{{- end -}}
{{- else -}}
{{- printf "%s:%s" $repo $tag -}}
{{- end -}}
{{- end -}}

{{/*
主容器定义。judge 的 privileged、api 的挂在卷、各服务的探针差异都从这里取值。
*/}}
{{- define "bluenet.container" -}}
- name: {{ .Values.containerName | default (include "bluenet.name" .) }}
  image: {{ include "bluenet.image" . }}
  imagePullPolicy: {{ .Values.image.pullPolicy }}
  {{- with .Values.command }}
  command:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.args }}
  args:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- if .Values.service.enabled }}
  ports:
    {{- range .Values.service.ports }}
    - name: {{ .name }}
      containerPort: {{ .targetPort | default .port }}
      protocol: {{ .protocol | default "TCP" }}
    {{- end }}
  {{- end }}
  {{- with .Values.env }}
  env:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.envFrom }}
  envFrom:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.volumeMounts }}
  volumeMounts:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.probes.liveness }}
  livenessProbe:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.probes.readiness }}
  readinessProbe:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.probes.startup }}
  startupProbe:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  {{- with .Values.containerSecurityContext }}
  securityContext:
    {{- toYaml . | nindent 4 }}
  {{- end }}
  resources:
    {{- toYaml .Values.resources | nindent 4 }}
{{- end -}}

{{/*
Pod 规格。Deployment / StatefulSet / CronJob 三种工作负载共用，保证调度约束、
卷挂载、安全上下文等逻辑只写一遍。
*/}}
{{- define "bluenet.podSpec" -}}
{{- with .Values.nodeSelector }}
nodeSelector:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.tolerations }}
tolerations:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.affinity }}
affinity:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.topologySpreadConstraints }}
topologySpreadConstraints:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.securityContext }}
securityContext:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.imagePullSecrets }}
imagePullSecrets:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- with .Values.priorityClassName }}
priorityClassName: {{ . }}
{{- end }}
terminationGracePeriodSeconds: {{ .Values.terminationGracePeriodSeconds | default 30 }}
{{- with .Values.initContainers }}
initContainers:
  {{- toYaml . | nindent 2 }}
{{- end }}
containers:
  {{- include "bluenet.container" . | nindent 2 }}
{{- with .Values.volumes }}
volumes:
  {{- toYaml . | nindent 2 }}
{{- end }}
{{- end -}}
