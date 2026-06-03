import request from "@/utils/request";

export function getJobList(params) {
  return request({ url: "/hr/jobs", method: "get", params });
}

export function saveJob(data) {
  return request({ url: "/hr/jobs", method: "post", data });
}
